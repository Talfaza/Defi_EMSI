package com.example.defi.entities;

import lombok.Data;

/**
 * DTO for risk prediction response from ML service
 */
@Data
public class RiskPredictionResponse {
    private boolean success;
    private Double riskScore;
    private String riskLevel;
    private String error;

    public static RiskPredictionResponse error(String message) {
        RiskPredictionResponse response = new RiskPredictionResponse();
        response.setSuccess(false);
        response.setError(message);
        return response;
    }

    public static RiskPredictionResponse success(Double riskScore, String riskLevel) {
        RiskPredictionResponse response = new RiskPredictionResponse();
        response.setSuccess(true);
        response.setRiskScore(riskScore);
        response.setRiskLevel(riskLevel);
        return response;
    }
}
