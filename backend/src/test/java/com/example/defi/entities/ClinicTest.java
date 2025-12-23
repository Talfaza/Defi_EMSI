package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClinicTest {

    @Test
    @DisplayName("Clinic getters and setters should work correctly")
    void clinicGettersSetters_shouldWorkCorrectly() {
        Clinic clinic = new Clinic();

        clinic.setId(1L);
        clinic.setName("Test Clinic");
        clinic.setAddress("456 Clinic Avenue");
        clinic.setLicenseNumber("LIC-12345");
        clinic.setEmail("clinic@test.com");
        clinic.setWalletAddress("0xClinic123456789");

        assertEquals(1L, clinic.getId());
        assertEquals("Test Clinic", clinic.getName());
        assertEquals("456 Clinic Avenue", clinic.getAddress());
        assertEquals("LIC-12345", clinic.getLicenseNumber());
        assertEquals("clinic@test.com", clinic.getEmail());
        assertEquals("0xClinic123456789", clinic.getWalletAddress());
    }

    @Test
    @DisplayName("Clinic paymentRequests should be settable")
    void clinicPaymentRequests_shouldBeSettable() {
        Clinic clinic = new Clinic();
        List<PaymentRequest> requests = new ArrayList<>();

        PaymentRequest request = new PaymentRequest();
        request.setRequestId("PR-001");
        requests.add(request);

        clinic.setPaymentRequests(requests);

        assertNotNull(clinic.getPaymentRequests());
        assertEquals(1, clinic.getPaymentRequests().size());
        assertEquals("PR-001", clinic.getPaymentRequests().get(0).getRequestId());
    }

    @Test
    @DisplayName("Clinic equals and hashCode should work with Lombok")
    void clinicEqualsHashCode_shouldWork() {
        Clinic clinic1 = new Clinic();
        clinic1.setId(1L);
        clinic1.setEmail("clinic@test.com");

        Clinic clinic2 = new Clinic();
        clinic2.setId(1L);
        clinic2.setEmail("clinic@test.com");

        assertEquals(clinic1, clinic2);
        assertEquals(clinic1.hashCode(), clinic2.hashCode());
    }

    @Test
    @DisplayName("Clinic toString should not throw exception")
    void clinicToString_shouldNotThrow() {
        Clinic clinic = new Clinic();
        clinic.setId(1L);
        clinic.setName("Test Clinic");

        assertDoesNotThrow(() -> clinic.toString());
        assertTrue(clinic.toString().contains("Test Clinic"));
    }

    @Test
    @DisplayName("Clinic with null fields should handle gracefully")
    void clinicWithNullFields_shouldHandleGracefully() {
        Clinic clinic = new Clinic();

        assertNull(clinic.getId());
        assertNull(clinic.getName());
        assertNull(clinic.getAddress());
        assertNull(clinic.getEmail());
        assertNull(clinic.getLicenseNumber());
        assertNull(clinic.getWalletAddress());
        assertNull(clinic.getPaymentRequests());
    }

    @Test
    @DisplayName("Clinic should not equal different clinic")
    void clinic_shouldNotEqualDifferentClinic() {
        Clinic clinic1 = new Clinic();
        clinic1.setId(1L);
        clinic1.setName("Clinic A");

        Clinic clinic2 = new Clinic();
        clinic2.setId(2L);
        clinic2.setName("Clinic B");

        assertNotEquals(clinic1, clinic2);
    }
}
