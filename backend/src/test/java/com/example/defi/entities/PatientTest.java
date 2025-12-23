package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    @DisplayName("Patient getters and setters should work correctly")
    void patientGettersSetters_shouldWorkCorrectly() {
        Patient patient = new Patient();

        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setCIN("AB123456");
        patient.setAddress("123 Test Street");
        patient.setWallet("0x1234567890abcdef");
        patient.setEmail("john.doe@test.com");
        patient.setPrivateKey("0xPrivateKey123");

        assertEquals(1L, patient.getId());
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals("AB123456", patient.getCIN());
        assertEquals("123 Test Street", patient.getAddress());
        assertEquals("0x1234567890abcdef", patient.getWallet());
        assertEquals("john.doe@test.com", patient.getEmail());
        assertEquals("0xPrivateKey123", patient.getPrivateKey());
    }

    @Test
    @DisplayName("Patient paymentRequests should be settable")
    void patientPaymentRequests_shouldBeSettable() {
        Patient patient = new Patient();
        List<PaymentRequest> requests = new ArrayList<>();

        PaymentRequest request = new PaymentRequest();
        request.setRequestId("PR-001");
        requests.add(request);

        patient.setPaymentRequests(requests);

        assertNotNull(patient.getPaymentRequests());
        assertEquals(1, patient.getPaymentRequests().size());
        assertEquals("PR-001", patient.getPaymentRequests().get(0).getRequestId());
    }

    @Test
    @DisplayName("Patient equals and hashCode should work with Lombok")
    void patientEqualsHashCode_shouldWork() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setEmail("test@test.com");

        Patient patient2 = new Patient();
        patient2.setId(1L);
        patient2.setEmail("test@test.com");

        assertEquals(patient1, patient2);
        assertEquals(patient1.hashCode(), patient2.hashCode());
    }

    @Test
    @DisplayName("Patient toString should not throw exception")
    void patientToString_shouldNotThrow() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");

        assertDoesNotThrow(() -> patient.toString());
        assertTrue(patient.toString().contains("John"));
    }

    @Test
    @DisplayName("Patient with null fields should handle gracefully")
    void patientWithNullFields_shouldHandleGracefully() {
        Patient patient = new Patient();

        assertNull(patient.getId());
        assertNull(patient.getFirstName());
        assertNull(patient.getLastName());
        assertNull(patient.getEmail());
        assertNull(patient.getWallet());
        assertNull(patient.getPrivateKey());
        assertNull(patient.getPaymentRequests());
    }
}
