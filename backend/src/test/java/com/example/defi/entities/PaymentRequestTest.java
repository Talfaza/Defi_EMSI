package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    @Test
    @DisplayName("PaymentRequest getters and setters should work correctly")
    void paymentRequestGettersSetters_shouldWorkCorrectly() {
        PaymentRequest request = new PaymentRequest();
        ZonedDateTime now = ZonedDateTime.now();

        request.setRequestId("PR-001");
        request.setServiceDescription("Medical consultation");
        request.setServiceCode("MED-101");
        request.setAmountDue(new BigDecimal("150.00"));
        request.setToken("ETH");
        request.setStatus(Status.UNPAID);
        request.setCreatedAt(now);
        request.setPaidAt(null);
        request.setTransactionHash(null);

        assertEquals("PR-001", request.getRequestId());
        assertEquals("Medical consultation", request.getServiceDescription());
        assertEquals("MED-101", request.getServiceCode());
        assertEquals(new BigDecimal("150.00"), request.getAmountDue());
        assertEquals("ETH", request.getToken());
        assertEquals(Status.UNPAID, request.getStatus());
        assertEquals(now, request.getCreatedAt());
        assertNull(request.getPaidAt());
        assertNull(request.getTransactionHash());
    }

    @Test
    @DisplayName("PaymentRequest should handle clinic and patient relationships")
    void paymentRequest_shouldHandleRelationships() {
        PaymentRequest request = new PaymentRequest();

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");

        Clinic clinic = new Clinic();
        clinic.setId(1L);
        clinic.setName("Test Clinic");

        request.setPatient(patient);
        request.setClinic(clinic);

        assertNotNull(request.getPatient());
        assertNotNull(request.getClinic());
        assertEquals("John", request.getPatient().getFirstName());
        assertEquals("Test Clinic", request.getClinic().getName());
    }

    @Test
    @DisplayName("PaymentRequest should handle PAID status with transaction details")
    void paymentRequest_shouldHandlePaidStatus() {
        PaymentRequest request = new PaymentRequest();
        ZonedDateTime paidAt = ZonedDateTime.now();

        request.setStatus(Status.PAID);
        request.setPaidAt(paidAt);
        request.setTransactionHash("0xTransaction123");

        assertEquals(Status.PAID, request.getStatus());
        assertEquals(paidAt, request.getPaidAt());
        assertEquals("0xTransaction123", request.getTransactionHash());
    }

    @Test
    @DisplayName("PaymentRequest equals should work correctly")
    void paymentRequestEquals_shouldWork() {
        PaymentRequest request1 = new PaymentRequest();
        request1.setRequestId("PR-001");
        request1.setAmountDue(new BigDecimal("100.00"));

        PaymentRequest request2 = new PaymentRequest();
        request2.setRequestId("PR-001");
        request2.setAmountDue(new BigDecimal("100.00"));

        assertEquals(request1, request2);
    }

    @Test
    @DisplayName("PaymentRequest toString should not throw")
    void paymentRequestToString_shouldNotThrow() {
        PaymentRequest request = new PaymentRequest();
        request.setRequestId("PR-001");
        request.setServiceDescription("Test Service");

        assertDoesNotThrow(() -> request.toString());
    }

    @Test
    @DisplayName("PaymentRequest with null fields should handle gracefully")
    void paymentRequestWithNullFields_shouldHandleGracefully() {
        PaymentRequest request = new PaymentRequest();

        assertNull(request.getRequestId());
        assertNull(request.getServiceDescription());
        assertNull(request.getAmountDue());
        assertNull(request.getStatus());
        assertNull(request.getPatient());
        assertNull(request.getClinic());
    }
}
