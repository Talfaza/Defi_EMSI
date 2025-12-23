package com.example.defi.services;

import com.example.defi.entities.Clinic;
import com.example.defi.entities.Patient;
import com.example.defi.entities.PaymentHistory;
import com.example.defi.entities.PaymentRequest;
import com.example.defi.repositories.PaymentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentHistoryServiceTest {

    @Mock
    private PaymentHistoryRepository repository;

    @InjectMocks
    private PaymentHistoryService paymentHistoryService;

    private PaymentRequest testRequest;
    private Clinic testClinic;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setEmail("clinic@test.com");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("patient@test.com");

        testRequest = new PaymentRequest();
        testRequest.setRequestId("req-123");
        testRequest.setServiceDescription("Dental Cleaning");
        testRequest.setAmountDue(new BigDecimal("0.05"));
        testRequest.setClinic(testClinic);
        testRequest.setPatient(testPatient);
    }

    @Test
    @DisplayName("logPayment() should create and save payment history")
    void logPayment_shouldCreateAndSavePaymentHistory() {
        PaymentHistory savedHistory = new PaymentHistory();
        savedHistory.setPaymentRequest(testRequest);
        savedHistory.setClinic(testClinic);
        savedHistory.setPatient(testPatient);

        when(repository.save(any(PaymentHistory.class))).thenReturn(savedHistory);

        PaymentHistory result = paymentHistoryService.logPayment(testRequest, testClinic, testPatient);

        assertNotNull(result);
        assertEquals(testRequest, result.getPaymentRequest());
        assertEquals(testClinic, result.getClinic());
        assertEquals(testPatient, result.getPatient());
        verify(repository, times(1)).save(any(PaymentHistory.class));
    }

    @Test
    @DisplayName("logPayment() should associate correct entities")
    void logPayment_shouldAssociateCorrectEntities() {
        when(repository.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentHistory result = paymentHistoryService.logPayment(testRequest, testClinic, testPatient);

        assertEquals(testRequest, result.getPaymentRequest());
        assertEquals(testClinic, result.getClinic());
        assertEquals(testPatient, result.getPatient());
    }

    @Test
    @DisplayName("logPayment() should call repository save")
    void logPayment_shouldCallRepositorySave() {
        when(repository.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentHistoryService.logPayment(testRequest, testClinic, testPatient);

        verify(repository, times(1)).save(any(PaymentHistory.class));
    }
}
