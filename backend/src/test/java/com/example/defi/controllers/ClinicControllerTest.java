package com.example.defi.controllers;

import com.example.defi.entities.Clinic;
import com.example.defi.services.ClinicService;
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

@WebMvcTest(ClinicController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClinicService clinicService;

    @Autowired
    private ObjectMapper objectMapper;

    private Clinic testClinic;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setEmail("clinic@test.com");
        testClinic.setAddress("456 Clinic Avenue");
        testClinic.setLicenseNumber("LIC-12345");
        testClinic.setWalletAddress("0xClinic123456789");
    }

    @Test
    @DisplayName("GET /clinics/all should return all clinics")
    void getAllClinics_shouldReturnAllClinics() throws Exception {
        Clinic anotherClinic = new Clinic();
        anotherClinic.setId(2L);
        anotherClinic.setName("Another Clinic");

        List<Clinic> clinics = Arrays.asList(testClinic, anotherClinic);
        when(clinicService.findAll()).thenReturn(clinics);

        mockMvc.perform(get("/clinics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Clinic"))
                .andExpect(jsonPath("$[1].name").value("Another Clinic"));

        verify(clinicService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /clinics/all should return empty list when no clinics")
    void getAllClinics_shouldReturnEmptyList() throws Exception {
        when(clinicService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/clinics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /clinics/add should create new clinic")
    void addClinic_shouldCreateClinic() throws Exception {
        when(clinicService.save(any(Clinic.class))).thenReturn(testClinic);

        mockMvc.perform(post("/clinics/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testClinic)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Clinic"))
                .andExpect(jsonPath("$.email").value("clinic@test.com"));

        verify(clinicService, times(1)).save(any(Clinic.class));
    }

    @Test
    @DisplayName("GET /clinics/{id} should return clinic by ID")
    void getClinicById_shouldReturnClinic() throws Exception {
        when(clinicService.findById(1L)).thenReturn(testClinic);

        mockMvc.perform(get("/clinics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Clinic"));

        verify(clinicService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /clinics/{id} should return null when clinic not found")
    void getClinicById_shouldReturnNull_whenNotFound() throws Exception {
        when(clinicService.findById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/clinics/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("DELETE /clinics/{id} should delete clinic")
    void deleteClinic_shouldDeleteClinic() throws Exception {
        doNothing().when(clinicService).delete(anyLong());

        mockMvc.perform(delete("/clinics/1"))
                .andExpect(status().isOk());

        verify(clinicService, times(1)).delete(1L);
    }
}
