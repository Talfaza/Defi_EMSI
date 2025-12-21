package com.example.defi.services;

import com.example.defi.entities.PaymentRequest;
import com.example.defi.entities.Status;
import com.example.defi.repositories.PaymentRequestRepository;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentRequestService {
    private final PaymentRequestRepository paymentRequestRepository;

    public PaymentRequestService(PaymentRequestRepository paymentRequestRepository) {
        this.paymentRequestRepository = paymentRequestRepository;
    }

    public List<PaymentRequest> findAll() {
        return paymentRequestRepository.findAll();
    }

    public PaymentRequest save(PaymentRequest request) {
        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        if (request.getCreatedAt() == null) {
            request.setCreatedAt(ZonedDateTime.now());
        }
        if (request.getStatus() == null) {
            request.setStatus(Status.UNPAID);
        }
        return paymentRequestRepository.save(request);
    }

    public PaymentRequest findById(String id) {
        return paymentRequestRepository.findById(id).orElse(null);
    }

    public void delete(String id) {
        paymentRequestRepository.deleteById(id);
    }

    public List<PaymentRequest> findByPatientId(Long patientId) {
        return paymentRequestRepository.findByPatientId(patientId);
    }

    public List<PaymentRequest> findByClinicId(Long clinicId) {
        return paymentRequestRepository.findByClinicId(clinicId);
    }

    public List<PaymentRequest> findPendingByPatientId(Long patientId) {
        return paymentRequestRepository.findByPatientIdAndStatus(patientId, Status.UNPAID);
    }

    public List<PaymentRequest> findPendingByClinicId(Long clinicId) {
        return paymentRequestRepository.findByClinicIdAndStatus(clinicId, Status.UNPAID);
    }

    public PaymentRequest markAsPaid(String requestId, String transactionHash) {
        PaymentRequest request = findById(requestId);
        if (request != null) {
            request.setStatus(Status.PAID);
            request.setPaidAt(ZonedDateTime.now());
            request.setTransactionHash(transactionHash);
            return paymentRequestRepository.save(request);
        }
        return null;
    }
}
