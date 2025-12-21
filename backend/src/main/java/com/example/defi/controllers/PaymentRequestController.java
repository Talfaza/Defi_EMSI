package com.example.defi.controllers;

import com.example.defi.entities.Clinic;
import com.example.defi.entities.Patient;
import com.example.defi.entities.PaymentRequest;
import com.example.defi.entities.Status;
import com.example.defi.repositories.ClinicRepository;
import com.example.defi.repositories.PatientRepository;
import com.example.defi.services.BlockchainService;
import com.example.defi.services.PaymentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final BlockchainService blockchainService;

    public PaymentRequestController(
            PaymentRequestService paymentRequestService,
            ClinicRepository clinicRepository,
            PatientRepository patientRepository,
            BlockchainService blockchainService) {
        this.paymentRequestService = paymentRequestService;
        this.clinicRepository = clinicRepository;
        this.patientRepository = patientRepository;
        this.blockchainService = blockchainService;
    }

    @GetMapping("/all")
    public List<PaymentRequest> getAllRequests() {
        return paymentRequestService.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPaymentRequest(@RequestBody Map<String, Object> body) {
        try {
            Long clinicId = Long.valueOf(body.get("clinicId").toString());
            String patientWallet = (String) body.get("patientWallet");
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            String description = (String) body.get("description");

            // Find clinic
            Clinic clinic = clinicRepository.findById(clinicId).orElse(null);
            if (clinic == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Clinic not found"));
            }

            // Find patient by wallet address (case-insensitive)
            Patient patient = patientRepository.findByWalletIgnoreCase(patientWallet).orElse(null);
            if (patient == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient with this wallet not found"));
            }

            PaymentRequest request = new PaymentRequest();
            request.setClinic(clinic);
            request.setPatient(patient);
            request.setAmountDue(amount);
            request.setServiceDescription(description);
            request.setStatus(Status.UNPAID);

            PaymentRequest saved = paymentRequestService.save(request);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public List<PaymentRequest> getPatientPayments(@PathVariable Long patientId) {
        return paymentRequestService.findByPatientId(patientId);
    }

    @GetMapping("/patient/{patientId}/pending")
    public List<PaymentRequest> getPatientPendingPayments(@PathVariable Long patientId) {
        return paymentRequestService.findPendingByPatientId(patientId);
    }

    @GetMapping("/clinic/{clinicId}")
    public List<PaymentRequest> getClinicPayments(@PathVariable Long clinicId) {
        return paymentRequestService.findByClinicId(clinicId);
    }

    @GetMapping("/clinic/{clinicId}/pending")
    public List<PaymentRequest> getClinicPendingPayments(@PathVariable Long clinicId) {
        return paymentRequestService.findPendingByClinicId(clinicId);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payRequest(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            // Get the payment request
            PaymentRequest request = paymentRequestService.findById(id);
            if (request == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.getStatus() == Status.PAID) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment already completed"));
            }

            Patient patient = request.getPatient();
            Clinic clinic = request.getClinic();

            // Check if patient has private key stored
            String privateKey = body.get("privateKey");
            if (privateKey == null || privateKey.isEmpty()) {
                privateKey = patient.getPrivateKey();
            }

            if (privateKey == null || privateKey.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Patient private key not provided. Please include 'privateKey' in request body."));
            }

            // Execute blockchain transaction
            String transactionHash = blockchainService.transferEth(
                    privateKey,
                    clinic.getWalletAddress(),
                    request.getAmountDue());

            // Mark as paid in database
            PaymentRequest updated = paymentRequestService.markAsPaid(id, transactionHash);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionHash", transactionHash,
                    "payment", updated));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public PaymentRequest getRequest(@PathVariable String id) {
        return paymentRequestService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable String id) {
        paymentRequestService.delete(id);
    }
}
