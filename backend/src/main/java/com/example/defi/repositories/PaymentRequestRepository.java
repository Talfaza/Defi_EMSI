package com.example.defi.repositories;

import com.example.defi.entities.PaymentRequest;
import com.example.defi.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, String> {
    List<PaymentRequest> findByPatientId(Long patientId);

    List<PaymentRequest> findByClinicId(Long clinicId);

    List<PaymentRequest> findByPatientIdAndStatus(Long patientId, Status status);

    List<PaymentRequest> findByClinicIdAndStatus(Long clinicId, Status status);
}
