package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskPredictionResponseTest {

    @Test
    @DisplayName("RiskPredictionResponse success factory method should work correctly")
    void success_shouldCreateSuccessResponse() {
        RiskPredictionResponse response = RiskPredictionResponse.success(0.3, "LOW");

        assertTrue(response.isSuccess());
        assertEquals(0.3, response.getRiskScore());
        assertEquals("LOW", response.getRiskLevel());
        assertNull(response.getError());
    }

    @Test
    @DisplayName("RiskPredictionResponse error factory method should work correctly")
    void error_shouldCreateErrorResponse() {
        RiskPredictionResponse response = RiskPredictionResponse.error("Patient not found");

        assertFalse(response.isSuccess());
        assertNull(response.getRiskScore());
        assertNull(response.getRiskLevel());
        assertEquals("Patient not found", response.getError());
    }

    @Test
    @DisplayName("RiskPredictionResponse getters and setters should work")
    void gettersSetters_shouldWork() {
        RiskPredictionResponse response = new RiskPredictionResponse();

        response.setSuccess(true);
        response.setRiskScore(0.75);
        response.setRiskLevel("HIGH");
        response.setError(null);

        assertTrue(response.isSuccess());
        assertEquals(0.75, response.getRiskScore());
        assertEquals("HIGH", response.getRiskLevel());
        assertNull(response.getError());
    }

    @Test
    @DisplayName("RiskPredictionResponse should handle different risk levels")
    void riskPredictionResponse_shouldHandleDifferentRiskLevels() {
        RiskPredictionResponse low = RiskPredictionResponse.success(0.2, "LOW");
        RiskPredictionResponse medium = RiskPredictionResponse.success(0.5, "MEDIUM");
        RiskPredictionResponse high = RiskPredictionResponse.success(0.8, "HIGH");

        assertEquals("LOW", low.getRiskLevel());
        assertEquals("MEDIUM", medium.getRiskLevel());
        assertEquals("HIGH", high.getRiskLevel());
    }

    @Test
    @DisplayName("RiskPredictionResponse equals should work correctly")
    void equals_shouldWorkCorrectly() {
        RiskPredictionResponse response1 = RiskPredictionResponse.success(0.5, "MEDIUM");
        RiskPredictionResponse response2 = RiskPredictionResponse.success(0.5, "MEDIUM");

        assertEquals(response1, response2);
    }

    @Test
    @DisplayName("RiskPredictionResponse toString should not throw")
    void toString_shouldNotThrow() {
        RiskPredictionResponse response = RiskPredictionResponse.success(0.5, "MEDIUM");

        assertDoesNotThrow(() -> response.toString());
        assertTrue(response.toString().contains("MEDIUM"));
    }
}
