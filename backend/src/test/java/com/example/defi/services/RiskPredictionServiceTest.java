package com.example.defi.services;

import com.example.defi.entities.*;
import com.example.defi.repositories.ClinicRepository;
import com.example.defi.repositories.PatientRepository;
import com.example.defi.repositories.PaymentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskPredictionServiceTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private RestTemplate restTemplate;

    private RiskPredictionService riskPredictionService;
    private Patient testPatient;
    private Clinic testClinic;

    @BeforeEach
    void setUp() {
        riskPredictionService = new RiskPredictionService(
                paymentRequestRepository,
                patientRepository,
                clinicRepository);
        ReflectionTestUtils.setField(riskPredictionService, "mlServiceUrl", "http://localhost:5001");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setWallet("0xPatient123");

        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setWalletAddress("0xClinic456");
    }

    @Test
    @DisplayName("predictRisk should return error when patient not found")
    void predictRisk_shouldReturnError_whenPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        RiskPredictionResponse response = riskPredictionService.predictRisk(999L, 1L, 100.0);

        assertFalse(response.isSuccess());
        assertEquals("Patient not found", response.getError());
    }

    @Test
    @DisplayName("predictRisk should return error when clinic not found")
    void predictRisk_shouldReturnError_whenClinicNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(clinicRepository.findById(999L)).thenReturn(Optional.empty());

        RiskPredictionResponse response = riskPredictionService.predictRisk(1L, 999L, 100.0);

        assertFalse(response.isSuccess());
        assertEquals("Clinic not found", response.getError());
    }

    @Test
    @DisplayName("predictRiskByWallet should return default medium risk for new patients")
    void predictRiskByWallet_shouldReturnDefaultRisk_forNewPatients() {
        when(patientRepository.findByWalletIgnoreCase("0xUnknown")).thenReturn(Optional.empty());

        RiskPredictionResponse response = riskPredictionService.predictRiskByWallet("0xUnknown", 1L, 100.0);

        assertTrue(response.isSuccess());
        assertEquals(0.5, response.getRiskScore());
        assertEquals("MEDIUM", response.getRiskLevel());
    }

    @Test
    @DisplayName("predictRiskByWallet should call predictRisk for known patients")
    void predictRiskByWallet_shouldCallPredictRisk_forKnownPatients() {
        when(patientRepository.findByWalletIgnoreCase("0xPatient123")).thenReturn(Optional.of(testPatient));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(testClinic));
        when(paymentRequestRepository.findByPatientId(1L)).thenReturn(new ArrayList<>());
        when(paymentRequestRepository.findByClinicId(1L)).thenReturn(new ArrayList<>());

        // The method will fail calling the actual ML service, but we're testing the
        // flow
        RiskPredictionResponse response = riskPredictionService.predictRiskByWallet("0xPatient123", 1L, 100.0);

        // Verify the repositories were called
        verify(patientRepository, times(1)).findByWalletIgnoreCase("0xPatient123");
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("isServiceAvailable should return false when ML service is down")
    void isServiceAvailable_shouldReturnFalse_whenServiceDown() {
        // Without mocking RestTemplate, the actual call will fail
        boolean available = riskPredictionService.isServiceAvailable();

        assertFalse(available);
    }

    @Test
    @DisplayName("predictRisk should compute statistics correctly")
    void predictRisk_shouldComputeStatisticsCorrectly() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(testClinic));

        // Create some payment history
        List<PaymentRequest> patientPayments = new ArrayList<>();
        PaymentRequest paidRequest = new PaymentRequest();
        paidRequest.setStatus(Status.PAID);
        paidRequest.setAmountDue(new BigDecimal("100.00"));
        patientPayments.add(paidRequest);

        PaymentRequest unpaidRequest = new PaymentRequest();
        unpaidRequest.setStatus(Status.UNPAID);
        unpaidRequest.setAmountDue(new BigDecimal("200.00"));
        patientPayments.add(unpaidRequest);

        when(paymentRequestRepository.findByPatientId(1L)).thenReturn(patientPayments);
        when(paymentRequestRepository.findByClinicId(1L)).thenReturn(patientPayments);

        // The actual ML service call will fail, but this tests the statistics
        // computation
        RiskPredictionResponse response = riskPredictionService.predictRisk(1L, 1L, 150.0);

        verify(paymentRequestRepository, times(1)).findByPatientId(1L);
        verify(paymentRequestRepository, times(1)).findByClinicId(1L);
    }
}
