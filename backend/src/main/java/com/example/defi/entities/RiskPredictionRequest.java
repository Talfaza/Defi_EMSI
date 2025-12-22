package com.example.defi.entities;

import lombok.Data;

/**
 * DTO for risk prediction request to ML service
 */
@Data
public class RiskPredictionRequest {
    private Double paymentAmount;
    private Integer paymentHour;
    private Integer paymentWeekday;
    private Integer paymentMonth;
    private Integer patientTotalPayments;
    private Integer patientFailedPayments;
    private Double patientAvgPaymentAmount;
    private Double clinicDefaultRate;
    private Integer paymentFailedBefore;
}
