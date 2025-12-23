package com.example.defi.controllers;

import com.example.defi.entities.*;
import com.example.defi.repositories.ClinicRepository;
import com.example.defi.repositories.PatientRepository;
import com.example.defi.services.BlockchainService;
import com.example.defi.services.PaymentRequestService;
import com.example.defi.services.RiskPredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRequestService paymentRequestService;

    @MockBean
    private ClinicRepository clinicRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private BlockchainService blockchainService;

    @MockBean
    private RiskPredictionService riskPredictionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest testPaymentRequest;
    private Patient testPatient;
    private Clinic testClinic;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setWallet("0xPatient123");
        testPatient.setEmail("patient@test.com");

        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setWalletAddress("0xClinic456");

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setRequestId("PR-001");
        testPaymentRequest.setPatient(testPatient);
        testPaymentRequest.setClinic(testClinic);
        testPaymentRequest.setAmountDue(new BigDecimal("100.00"));
        testPaymentRequest.setServiceDescription("Medical consultation");
        testPaymentRequest.setStatus(Status.UNPAID);
    }

    @Test
    @DisplayName("GET /api/payments/all should return all payment requests")
    @WithMockUser
    void getAllRequests_shouldReturnAllPaymentRequests() throws Exception {
        List<PaymentRequest> requests = Arrays.asList(testPaymentRequest);
        when(paymentRequestService.findAll()).thenReturn(requests);

        mockMvc.perform(get("/api/payments/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requestId").value("PR-001"));

        verify(paymentRequestService, times(1)).findAll();
    }

    @Test
    @DisplayName("POST /api/payments/create should create payment request")
    @WithMockUser
    void createPaymentRequest_shouldCreateRequest() throws Exception {
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(testClinic));
        when(patientRepository.findByWalletIgnoreCase("0xPatient123")).thenReturn(Optional.of(testPatient));
        when(paymentRequestService.save(any(PaymentRequest.class))).thenReturn(testPaymentRequest);

        Map<String, Object> body = new HashMap<>();
        body.put("clinicId", 1);
        body.put("patientWallet", "0xPatient123");
        body.put("amount", "100.00");
        body.put("description", "Medical consultation");

        mockMvc.perform(post("/api/payments/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("PR-001"));

        verify(paymentRequestService, times(1)).save(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("POST /api/payments/create should return error when clinic not found")
    @WithMockUser
    void createPaymentRequest_shouldReturnError_whenClinicNotFound() throws Exception {
        when(clinicRepository.findById(anyLong())).thenReturn(Optional.empty());

        Map<String, Object> body = new HashMap<>();
        body.put("clinicId", 999);
        body.put("patientWallet", "0xPatient123");
        body.put("amount", "100.00");
        body.put("description", "Test");

        mockMvc.perform(post("/api/payments/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Clinic not found"));
    }

    @Test
    @DisplayName("POST /api/payments/create should return error when patient not found")
    @WithMockUser
    void createPaymentRequest_shouldReturnError_whenPatientNotFound() throws Exception {
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(testClinic));
        when(patientRepository.findByWalletIgnoreCase(anyString())).thenReturn(Optional.empty());

        Map<String, Object> body = new HashMap<>();
        body.put("clinicId", 1);
        body.put("patientWallet", "0xUnknown");
        body.put("amount", "100.00");
        body.put("description", "Test");

        mockMvc.perform(post("/api/payments/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Patient with this wallet not found"));
    }

    @Test
    @DisplayName("GET /api/payments/patient/{patientId} should return patient payments")
    @WithMockUser
    void getPatientPayments_shouldReturnPayments() throws Exception {
        when(paymentRequestService.findByPatientId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        mockMvc.perform(get("/api/payments/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(paymentRequestService, times(1)).findByPatientId(1L);
    }

    @Test
    @DisplayName("GET /api/payments/patient/{patientId}/pending should return pending payments")
    @WithMockUser
    void getPatientPendingPayments_shouldReturnPendingPayments() throws Exception {
        when(paymentRequestService.findPendingByPatientId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        mockMvc.perform(get("/api/payments/patient/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(paymentRequestService, times(1)).findPendingByPatientId(1L);
    }

    @Test
    @DisplayName("GET /api/payments/clinic/{clinicId} should return clinic payments")
    @WithMockUser
    void getClinicPayments_shouldReturnPayments() throws Exception {
        when(paymentRequestService.findByClinicId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        mockMvc.perform(get("/api/payments/clinic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(paymentRequestService, times(1)).findByClinicId(1L);
    }

    @Test
    @DisplayName("GET /api/payments/clinic/{clinicId}/pending should return pending clinic payments")
    @WithMockUser
    void getClinicPendingPayments_shouldReturnPendingPayments() throws Exception {
        when(paymentRequestService.findPendingByClinicId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        mockMvc.perform(get("/api/payments/clinic/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(paymentRequestService, times(1)).findPendingByClinicId(1L);
    }

    @Test
    @DisplayName("POST /api/payments/risk should return risk prediction")
    @WithMockUser
    void getRiskPrediction_shouldReturnPrediction() throws Exception {
        RiskPredictionResponse response = RiskPredictionResponse.success(0.3, "LOW");
        when(riskPredictionService.predictRiskByWallet(anyString(), anyLong(), anyDouble())).thenReturn(response);

        Map<String, Object> body = new HashMap<>();
        body.put("patientWallet", "0xPatient123");
        body.put("clinicId", 1);
        body.put("amount", 100.0);

        mockMvc.perform(post("/api/payments/risk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(riskPredictionService, times(1)).predictRiskByWallet("0xPatient123", 1L, 100.0);
    }

    @Test
    @DisplayName("GET /api/payments/risk/health should check ML service health")
    @WithMockUser
    void checkMlServiceHealth_shouldReturnAvailability() throws Exception {
        when(riskPredictionService.isServiceAvailable()).thenReturn(true);

        mockMvc.perform(get("/api/payments/risk/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(riskPredictionService, times(1)).isServiceAvailable();
    }

    @Test
    @DisplayName("GET /api/payments/{id} should return payment request by ID")
    @WithMockUser
    void getRequest_shouldReturnPaymentRequest() throws Exception {
        when(paymentRequestService.findById("PR-001")).thenReturn(testPaymentRequest);

        mockMvc.perform(get("/api/payments/PR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("PR-001"));

        verify(paymentRequestService, times(1)).findById("PR-001");
    }

    @Test
    @DisplayName("DELETE /api/payments/{id} should delete payment request")
    @WithMockUser
    void deleteRequest_shouldDeletePaymentRequest() throws Exception {
        doNothing().when(paymentRequestService).delete(anyString());

        mockMvc.perform(delete("/api/payments/PR-001")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(paymentRequestService, times(1)).delete("PR-001");
    }

    @Test
    @DisplayName("POST /api/payments/{id}/pay should process payment")
    @WithMockUser
    void payRequest_shouldProcessPayment() throws Exception {
        testPatient.setPrivateKey("0xPrivateKey123");
        testPaymentRequest.setPatient(testPatient);

        PaymentRequest paidRequest = new PaymentRequest();
        paidRequest.setRequestId("PR-001");
        paidRequest.setStatus(Status.PAID);
        paidRequest.setTransactionHash("0xTxHash123");

        when(paymentRequestService.findById("PR-001")).thenReturn(testPaymentRequest);
        when(blockchainService.transferEth(anyString(), anyString(), any(BigDecimal.class))).thenReturn("0xTxHash123");
        when(paymentRequestService.markAsPaid("PR-001", "0xTxHash123")).thenReturn(paidRequest);

        Map<String, String> body = new HashMap<>();
        body.put("privateKey", "0xPrivateKey123");

        mockMvc.perform(post("/api/payments/PR-001/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionHash").value("0xTxHash123"));
    }

    @Test
    @DisplayName("POST /api/payments/{id}/pay should return 404 when payment request not found")
    @WithMockUser
    void payRequest_shouldReturn404_whenNotFound() throws Exception {
        when(paymentRequestService.findById(anyString())).thenReturn(null);

        Map<String, String> body = new HashMap<>();
        body.put("privateKey", "0xPrivateKey123");

        mockMvc.perform(post("/api/payments/PR-999/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/payments/{id}/pay should return error when already paid")
    @WithMockUser
    void payRequest_shouldReturnError_whenAlreadyPaid() throws Exception {
        testPaymentRequest.setStatus(Status.PAID);
        when(paymentRequestService.findById("PR-001")).thenReturn(testPaymentRequest);

        Map<String, String> body = new HashMap<>();
        body.put("privateKey", "0xPrivateKey123");

        mockMvc.perform(post("/api/payments/PR-001/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Payment already completed"));
    }
}
