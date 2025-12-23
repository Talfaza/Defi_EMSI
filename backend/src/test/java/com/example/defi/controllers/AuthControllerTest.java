package com.example.defi.controllers;

import com.example.defi.entities.Clinic;
import com.example.defi.entities.Patient;
import com.example.defi.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private Clinic testClinic;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
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
    @DisplayName("POST /auth/signup/clinic should register new clinic")
    @WithMockUser
    void signupClinic_shouldRegisterClinic() throws Exception {
        when(authService.signupClinic(any(Clinic.class), anyString())).thenReturn(testClinic);

        Map<String, Object> body = new HashMap<>();
        body.put("email", "clinic@test.com");
        body.put("password", "password123");
        body.put("name", "Test Clinic");
        body.put("address", "123 Clinic Street");
        body.put("licenseNumber", "LIC-12345");
        body.put("walletAddress", "0xClinic123");

        mockMvc.perform(post("/auth/signup/clinic")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Clinic"))
                .andExpect(jsonPath("$.email").value("clinic@test.com"));

        verify(authService, times(1)).signupClinic(any(Clinic.class), eq("password123"));
    }

    @Test
    @DisplayName("POST /auth/signup/patient should register new patient")
    @WithMockUser
    void signupPatient_shouldRegisterPatient() throws Exception {
        when(authService.signupPatient(any(Patient.class), anyString())).thenReturn(testPatient);

        Map<String, Object> body = new HashMap<>();
        body.put("email", "patient@test.com");
        body.put("password", "password123");
        body.put("firstName", "John");
        body.put("lastName", "Doe");
        body.put("address", "456 Patient Lane");
        body.put("cin", "AB123456");
        body.put("wallet", "0xPatient456");

        mockMvc.perform(post("/auth/signup/patient")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(authService, times(1)).signupPatient(any(Patient.class), eq("password123"));
    }

    @Test
    @DisplayName("POST /auth/login should authenticate user and return tokens")
    @WithMockUser
    void login_shouldAuthenticateAndReturnTokens() throws Exception {
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("idToken", "test-id-token");
        loginResponse.put("expiresIn", "3600");
        loginResponse.put("refreshToken", "test-refresh-token");
        loginResponse.put("type", "PATIENT");
        loginResponse.put("userId", 1L);
        loginResponse.put("name", "John");
        loginResponse.put("walletAddress", "0xPatient456");

        when(authService.login("patient@test.com", "password123")).thenReturn(loginResponse);

        Map<String, String> body = new HashMap<>();
        body.put("email", "patient@test.com");
        body.put("password", "password123");

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idToken").value("test-id-token"))
                .andExpect(jsonPath("$.type").value("PATIENT"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(authService, times(1)).login("patient@test.com", "password123");
    }

    @Test
    @DisplayName("POST /auth/login with clinic credentials should return clinic info")
    @WithMockUser
    void login_withClinicCredentials_shouldReturnClinicInfo() throws Exception {
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("idToken", "clinic-id-token");
        loginResponse.put("expiresIn", "3600");
        loginResponse.put("refreshToken", "clinic-refresh-token");
        loginResponse.put("type", "CLINIC");
        loginResponse.put("userId", 1L);
        loginResponse.put("name", "Test Clinic");
        loginResponse.put("walletAddress", "0xClinic123");

        when(authService.login("clinic@test.com", "password123")).thenReturn(loginResponse);

        Map<String, String> body = new HashMap<>();
        body.put("email", "clinic@test.com");
        body.put("password", "password123");

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CLINIC"))
                .andExpect(jsonPath("$.name").value("Test Clinic"));

        verify(authService, times(1)).login("clinic@test.com", "password123");
    }
}
