package com.example.defi.controllers;

import com.example.defi.entities.Patient;
import com.example.defi.services.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("john.doe@test.com");
        testPatient.setWallet("0x1234567890abcdef");
        testPatient.setCIN("AB123456");
        testPatient.setAddress("123 Test Street");
    }

    @Test
    @DisplayName("GET /api/patients/all should return all patients")
    void getAllPatients_shouldReturnAllPatients() throws Exception {
        Patient anotherPatient = new Patient();
        anotherPatient.setId(2L);
        anotherPatient.setFirstName("Jane");
        anotherPatient.setLastName("Smith");

        List<Patient> patients = Arrays.asList(testPatient, anotherPatient);
        when(patientService.findAll()).thenReturn(patients);

        mockMvc.perform(get("/api/patients/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(patientService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/patients/all should return empty list when no patients")
    void getAllPatients_shouldReturnEmptyList() throws Exception {
        when(patientService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/patients/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/patients/add should create new patient")
    void addPatient_shouldCreatePatient() throws Exception {
        when(patientService.save(any(Patient.class))).thenReturn(testPatient);

        mockMvc.perform(post("/api/patients/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(patientService, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("GET /api/patients/{id} should return patient by ID")
    void getPatientById_shouldReturnPatient() throws Exception {
        when(patientService.findById(1L)).thenReturn(testPatient);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(patientService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/patients/{id} should return null when patient not found")
    void getPatientById_shouldReturnNull_whenNotFound() throws Exception {
        when(patientService.findById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} should delete patient")
    void deletePatient_shouldDeletePatient() throws Exception {
        doNothing().when(patientService).delete(anyLong());

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isOk());

        verify(patientService, times(1)).delete(1L);
    }
}
