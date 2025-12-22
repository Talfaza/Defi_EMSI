package com.example.defi.services;

import com.example.defi.entities.Clinic;
import com.example.defi.entities.Patient;
import com.example.defi.entities.PaymentRequest;
import com.example.defi.entities.Status;
import com.example.defi.repositories.PaymentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRequestServiceTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @InjectMocks
    private PaymentRequestService paymentRequestService;

    private PaymentRequest testPaymentRequest;
    private Clinic testClinic;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setEmail("clinic@test.com");
        testClinic.setWalletAddress("0xCl1n1c000000000000000000000000000000001");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("patient@test.com");
        testPatient.setWallet("0xP4t13nt00000000000000000000000000000001");

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setRequestId(UUID.randomUUID().toString());
        testPaymentRequest.setServiceDescription("Dental Cleaning");
        testPaymentRequest.setAmountDue(new BigDecimal("0.05"));
        testPaymentRequest.setStatus(Status.UNPAID);
        testPaymentRequest.setCreatedAt(ZonedDateTime.now());
        testPaymentRequest.setClinic(testClinic);
        testPaymentRequest.setPatient(testPatient);
    }

    @Test
    @DisplayName("save() should generate UUID if requestId is null")
    void save_shouldGenerateUUID_whenRequestIdIsNull() {
        PaymentRequest newRequest = new PaymentRequest();
        newRequest.setServiceDescription("Checkup");
        newRequest.setAmountDue(new BigDecimal("0.1"));
        newRequest.setClinic(testClinic);
        newRequest.setPatient(testPatient);

        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(invocation -> {
            PaymentRequest arg = invocation.getArgument(0);
            return arg;
        });

        PaymentRequest savedRequest = paymentRequestService.save(newRequest);

        assertNotNull(savedRequest.getRequestId());
        assertFalse(savedRequest.getRequestId().isEmpty());
        verify(paymentRequestRepository, times(1)).save(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("save() should set status to UNPAID if null")
    void save_shouldSetUnpaidStatus_whenStatusIsNull() {
        PaymentRequest newRequest = new PaymentRequest();
        newRequest.setServiceDescription("Consultation");
        newRequest.setAmountDue(new BigDecimal("0.2"));

        when(paymentRequestRepository.save(any(PaymentRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest savedRequest = paymentRequestService.save(newRequest);

        assertEquals(Status.UNPAID, savedRequest.getStatus());
    }

    @Test
    @DisplayName("save() should set createdAt if null")
    void save_shouldSetCreatedAt_whenCreatedAtIsNull() {
        PaymentRequest newRequest = new PaymentRequest();
        newRequest.setServiceDescription("X-Ray");
        newRequest.setAmountDue(new BigDecimal("0.3"));

        when(paymentRequestRepository.save(any(PaymentRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest savedRequest = paymentRequestService.save(newRequest);

        assertNotNull(savedRequest.getCreatedAt());
    }

    @Test
    @DisplayName("findById() should return PaymentRequest when found")
    void findById_shouldReturnPaymentRequest_whenFound() {
        when(paymentRequestRepository.findById(anyString())).thenReturn(Optional.of(testPaymentRequest));

        PaymentRequest found = paymentRequestService.findById("test-id");

        assertNotNull(found);
        assertEquals(testPaymentRequest.getServiceDescription(), found.getServiceDescription());
    }

    @Test
    @DisplayName("findById() should return null when not found")
    void findById_shouldReturnNull_whenNotFound() {
        when(paymentRequestRepository.findById(anyString())).thenReturn(Optional.empty());

        PaymentRequest found = paymentRequestService.findById("non-existent-id");

        assertNull(found);
    }

    @Test
    @DisplayName("findAll() should return all payment requests")
    void findAll_shouldReturnAllPaymentRequests() {
        PaymentRequest anotherRequest = new PaymentRequest();
        anotherRequest.setRequestId(UUID.randomUUID().toString());
        anotherRequest.setServiceDescription("Follow-up");

        when(paymentRequestRepository.findAll()).thenReturn(Arrays.asList(testPaymentRequest, anotherRequest));

        List<PaymentRequest> requests = paymentRequestService.findAll();

        assertEquals(2, requests.size());
        verify(paymentRequestRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findByPatientId() should return patient's payment requests")
    void findByPatientId_shouldReturnPatientPaymentRequests() {
        when(paymentRequestRepository.findByPatientId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        List<PaymentRequest> requests = paymentRequestService.findByPatientId(1L);

        assertEquals(1, requests.size());
        assertEquals(testPaymentRequest.getPatient().getId(), requests.get(0).getPatient().getId());
    }

    @Test
    @DisplayName("findByClinicId() should return clinic's payment requests")
    void findByClinicId_shouldReturnClinicPaymentRequests() {
        when(paymentRequestRepository.findByClinicId(1L)).thenReturn(Arrays.asList(testPaymentRequest));

        List<PaymentRequest> requests = paymentRequestService.findByClinicId(1L);

        assertEquals(1, requests.size());
        assertEquals(testPaymentRequest.getClinic().getId(), requests.get(0).getClinic().getId());
    }

    @Test
    @DisplayName("findPendingByPatientId() should return only UNPAID requests")
    void findPendingByPatientId_shouldReturnOnlyUnpaidRequests() {
        when(paymentRequestRepository.findByPatientIdAndStatus(1L, Status.UNPAID))
                .thenReturn(Arrays.asList(testPaymentRequest));

        List<PaymentRequest> requests = paymentRequestService.findPendingByPatientId(1L);

        assertEquals(1, requests.size());
        assertEquals(Status.UNPAID, requests.get(0).getStatus());
    }

    @Test
    @DisplayName("markAsPaid() should update status and set transaction hash")
    void markAsPaid_shouldUpdateStatusAndSetTransactionHash() {
        String txHash = "0xabc123def456789...";

        when(paymentRequestRepository.findById(testPaymentRequest.getRequestId()))
                .thenReturn(Optional.of(testPaymentRequest));
        when(paymentRequestRepository.save(any(PaymentRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest paidRequest = paymentRequestService.markAsPaid(
                testPaymentRequest.getRequestId(), txHash);

        assertNotNull(paidRequest);
        assertEquals(Status.PAID, paidRequest.getStatus());
        assertEquals(txHash, paidRequest.getTransactionHash());
        assertNotNull(paidRequest.getPaidAt());
    }

    @Test
    @DisplayName("markAsPaid() should return null if request not found")
    void markAsPaid_shouldReturnNull_whenRequestNotFound() {
        when(paymentRequestRepository.findById(anyString())).thenReturn(Optional.empty());

        PaymentRequest result = paymentRequestService.markAsPaid("non-existent", "0x...");

        assertNull(result);
        verify(paymentRequestRepository, never()).save(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("delete() should call repository delete")
    void delete_shouldCallRepositoryDelete() {
        doNothing().when(paymentRequestRepository).deleteById(anyString());

        paymentRequestService.delete("test-id");

        verify(paymentRequestRepository, times(1)).deleteById("test-id");
    }
}
