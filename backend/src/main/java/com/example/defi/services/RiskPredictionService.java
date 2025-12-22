package com.example.defi.services;

import com.example.defi.entities.*;
import com.example.defi.repositories.PaymentRequestRepository;
import com.example.defi.repositories.PatientRepository;
import com.example.defi.repositories.ClinicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calling the ML model to predict payment default risk
 */
@Service
public class RiskPredictionService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:5001}")
    private String mlServiceUrl;

    public RiskPredictionService(
            PaymentRequestRepository paymentRequestRepository,
            PatientRepository patientRepository,
            ClinicRepository clinicRepository) {
        this.paymentRequestRepository = paymentRequestRepository;
        this.patientRepository = patientRepository;
        this.clinicRepository = clinicRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Predict risk for a new payment request
     */
    public RiskPredictionResponse predictRisk(Long patientId, Long clinicId, Double paymentAmount) {
        try {
            // Get patient
            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
                return RiskPredictionResponse.error("Patient not found");
            }

            // Get clinic
            Clinic clinic = clinicRepository.findById(clinicId).orElse(null);
            if (clinic == null) {
                return RiskPredictionResponse.error("Clinic not found");
            }

            // Compute patient statistics
            List<PaymentRequest> patientPayments = paymentRequestRepository.findByPatientId(patientId);
            int totalPayments = patientPayments.size();
            int failedPayments = (int) patientPayments.stream()
                    .filter(p -> p.getStatus() == Status.UNPAID)
                    .count();
            double avgPaymentAmount = patientPayments.stream()
                    .mapToDouble(p -> p.getAmountDue() != null ? p.getAmountDue().doubleValue() : 0)
                    .average()
                    .orElse(0.0);
            boolean hasFailedBefore = failedPayments > 0;

            // Compute clinic default rate
            List<PaymentRequest> clinicPayments = paymentRequestRepository.findByClinicId(clinicId);
            double clinicDefaultRate = 0.0;
            if (!clinicPayments.isEmpty()) {
                long clinicFailedCount = clinicPayments.stream()
                        .filter(p -> p.getStatus() == Status.UNPAID)
                        .count();
                clinicDefaultRate = (double) clinicFailedCount / clinicPayments.size();
            }

            // Get current time for temporal features
            LocalDateTime now = LocalDateTime.now();

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("payment_amount", paymentAmount);
            requestBody.put("payment_hour", now.getHour());
            requestBody.put("payment_weekday", now.getDayOfWeek().getValue() % 7);
            requestBody.put("payment_month", now.getMonthValue());
            requestBody.put("patient_total_payments", totalPayments);
            requestBody.put("patient_failed_payments", failedPayments);
            requestBody.put("patient_avg_payment_amount", avgPaymentAmount);
            requestBody.put("clinic_default_rate", clinicDefaultRate);
            requestBody.put("payment_failed_before", hasFailedBefore ? 1 : 0);

            // Call ML service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    mlServiceUrl + "/predict",
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = response.getBody();
                Boolean success = (Boolean) body.get("success");
                if (success != null && success) {
                    Double riskScore = ((Number) body.get("risk_score")).doubleValue();
                    String riskLevel = (String) body.get("risk_level");
                    return RiskPredictionResponse.success(riskScore, riskLevel);
                } else {
                    return RiskPredictionResponse.error((String) body.get("error"));
                }
            } else {
                return RiskPredictionResponse.error("ML service returned error");
            }

        } catch (Exception e) {
            return RiskPredictionResponse.error("Failed to get risk prediction: " + e.getMessage());
        }
    }

    /**
     * Predict risk using patient wallet address instead of ID
     */
    public RiskPredictionResponse predictRiskByWallet(String patientWallet, Long clinicId, Double paymentAmount) {
        Patient patient = patientRepository.findByWalletIgnoreCase(patientWallet).orElse(null);
        if (patient == null) {
            // Return default medium risk for new patients
            return RiskPredictionResponse.success(0.5, "MEDIUM");
        }
        return predictRisk(patient.getId(), clinicId, paymentAmount);
    }

    /**
     * Check if ML service is available
     */
    public boolean isServiceAvailable() {
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    mlServiceUrl + "/health",
                    Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
