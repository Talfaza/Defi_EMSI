package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentHistoryTest {

    @Test
    @DisplayName("PaymentHistory getters and setters should work correctly")
    void paymentHistoryGettersSetters_shouldWorkCorrectly() {
        PaymentHistory history = new PaymentHistory();

        history.setIdPHistory(1L);
        history.setTransactionHash("0xTransaction123");
        history.setBlockHash("0xBlock456");
        history.setBlockNumber(12345L);

        assertEquals(1L, history.getIdPHistory());
        assertEquals("0xTransaction123", history.getTransactionHash());
        assertEquals("0xBlock456", history.getBlockHash());
        assertEquals(12345L, history.getBlockNumber());
    }

    @Test
    @DisplayName("PaymentHistory should handle patient relationship")
    void paymentHistory_shouldHandlePatientRelationship() {
        PaymentHistory history = new PaymentHistory();

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");

        history.setPatient(patient);

        assertNotNull(history.getPatient());
        assertEquals("John", history.getPatient().getFirstName());
    }

    @Test
    @DisplayName("PaymentHistory should handle clinic relationship")
    void paymentHistory_shouldHandleClinicRelationship() {
        PaymentHistory history = new PaymentHistory();

        Clinic clinic = new Clinic();
        clinic.setId(1L);
        clinic.setName("Test Clinic");

        history.setClinic(clinic);

        assertNotNull(history.getClinic());
        assertEquals("Test Clinic", history.getClinic().getName());
    }

    @Test
    @DisplayName("PaymentHistory should handle paymentRequest relationship")
    void paymentHistory_shouldHandlePaymentRequestRelationship() {
        PaymentHistory history = new PaymentHistory();

        PaymentRequest request = new PaymentRequest();
        request.setRequestId("PR-001");

        history.setPaymentRequest(request);

        assertNotNull(history.getPaymentRequest());
        assertEquals("PR-001", history.getPaymentRequest().getRequestId());
    }

    @Test
    @DisplayName("PaymentHistory equals should work correctly")
    void paymentHistoryEquals_shouldWork() {
        PaymentHistory history1 = new PaymentHistory();
        history1.setIdPHistory(1L);
        history1.setTransactionHash("0xTransaction123");

        PaymentHistory history2 = new PaymentHistory();
        history2.setIdPHistory(1L);
        history2.setTransactionHash("0xTransaction123");

        assertEquals(history1, history2);
    }

    @Test
    @DisplayName("PaymentHistory toString should not throw")
    void paymentHistoryToString_shouldNotThrow() {
        PaymentHistory history = new PaymentHistory();
        history.setIdPHistory(1L);
        history.setTransactionHash("0xTransaction123");

        assertDoesNotThrow(() -> history.toString());
    }

    @Test
    @DisplayName("PaymentHistory with null fields should handle gracefully")
    void paymentHistoryWithNullFields_shouldHandleGracefully() {
        PaymentHistory history = new PaymentHistory();

        assertNull(history.getIdPHistory());
        assertNull(history.getTransactionHash());
        assertNull(history.getBlockHash());
        assertNull(history.getBlockNumber());
        assertNull(history.getPatient());
        assertNull(history.getClinic());
        assertNull(history.getPaymentRequest());
    }
}
