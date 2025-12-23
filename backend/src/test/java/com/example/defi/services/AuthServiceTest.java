package com.example.defi.services;

import com.example.defi.entities.Clinic;
import com.example.defi.entities.Patient;
import com.example.defi.repositories.ClinicRepository;
import com.example.defi.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    private Clinic testClinic;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "firebaseApiKey", "test-api-key");

        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setEmail("clinic@test.com");
        testClinic.setAddress("123 Clinic Street");
        testClinic.setLicenseNumber("LIC-12345");
        testClinic.setWalletAddress("0xClinic123");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("patient@test.com");
        testPatient.setAddress("456 Patient Lane");
        testPatient.setCIN("AB123456");
        testPatient.setWallet("0xPatient456");
    }

    @Test
    @DisplayName("login should return patient info when patient logs in")
    void login_shouldReturnPatientInfo_whenPatientLogsIn() {
        // Mock Firebase response
        Map<String, Object> firebaseResponse = new HashMap<>();
        firebaseResponse.put("idToken", "test-id-token");
        firebaseResponse.put("expiresIn", "3600");
        firebaseResponse.put("refreshToken", "test-refresh-token");

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(firebaseResponse));

        // Inject mocked RestTemplate
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate);

        when(clinicRepository.findByEmail("patient@test.com")).thenReturn(Optional.empty());
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(testPatient));

        Map<String, Object> result = authService.login("patient@test.com", "password123");

        assertEquals("test-id-token", result.get("idToken"));
        assertEquals("PATIENT", result.get("type"));
        assertEquals(1L, result.get("userId"));
        assertEquals("John", result.get("name"));
        assertEquals("0xPatient456", result.get("walletAddress"));
    }

    @Test
    @DisplayName("login should return clinic info when clinic logs in")
    void login_shouldReturnClinicInfo_whenClinicLogsIn() {
        // Mock Firebase response
        Map<String, Object> firebaseResponse = new HashMap<>();
        firebaseResponse.put("idToken", "clinic-id-token");
        firebaseResponse.put("expiresIn", "3600");
        firebaseResponse.put("refreshToken", "clinic-refresh-token");

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(firebaseResponse));

        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate);

        when(clinicRepository.findByEmail("clinic@test.com")).thenReturn(Optional.of(testClinic));
        when(patientRepository.findByEmail("clinic@test.com")).thenReturn(Optional.empty());

        Map<String, Object> result = authService.login("clinic@test.com", "password123");

        assertEquals("clinic-id-token", result.get("idToken"));
        assertEquals("CLINIC", result.get("type"));
        assertEquals(1L, result.get("userId"));
        assertEquals("Test Clinic", result.get("name"));
        assertEquals("0xClinic123", result.get("walletAddress"));
    }

    @Test
    @DisplayName("login should throw exception when user not in database")
    void login_shouldThrowException_whenUserNotInDatabase() {
        // Mock Firebase response
        Map<String, Object> firebaseResponse = new HashMap<>();
        firebaseResponse.put("idToken", "unknown-id-token");
        firebaseResponse.put("expiresIn", "3600");
        firebaseResponse.put("refreshToken", "unknown-refresh-token");

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(firebaseResponse));

        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate);

        when(clinicRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        when(patientRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login("unknown@test.com", "password123"));

        assertEquals("User authenticated but not found in database", exception.getMessage());
    }
}
