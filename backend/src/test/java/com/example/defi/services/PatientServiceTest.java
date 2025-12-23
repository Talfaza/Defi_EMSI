package com.example.defi.services;

import com.example.defi.entities.Patient;
import com.example.defi.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("patient@test.com");
        testPatient.setWallet("0xP4t13nt00000000000000000000000000000001");
    }

    @Test
    @DisplayName("findAll() should return all patients")
    void findAll_shouldReturnAllPatients() {
        Patient anotherPatient = new Patient();
        anotherPatient.setId(2L);
        anotherPatient.setFirstName("Jane");
        anotherPatient.setLastName("Smith");

        when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient, anotherPatient));

        List<Patient> patients = patientService.findAll();

        assertEquals(2, patients.size());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll() should return empty list when no patients")
    void findAll_shouldReturnEmptyList_whenNoPatients() {
        when(patientRepository.findAll()).thenReturn(Arrays.asList());

        List<Patient> patients = patientService.findAll();

        assertTrue(patients.isEmpty());
    }

    @Test
    @DisplayName("save() should save and return patient")
    void save_shouldSaveAndReturnPatient() {
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        Patient savedPatient = patientService.save(testPatient);

        assertNotNull(savedPatient);
        assertEquals(testPatient.getId(), savedPatient.getId());
        assertEquals(testPatient.getFirstName(), savedPatient.getFirstName());
        verify(patientRepository, times(1)).save(testPatient);
    }

    @Test
    @DisplayName("findById() should return patient when found")
    void findById_shouldReturnPatient_whenFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        Patient found = patientService.findById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("John", found.getFirstName());
    }

    @Test
    @DisplayName("findById() should return null when not found")
    void findById_shouldReturnNull_whenNotFound() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        Patient found = patientService.findById(999L);

        assertNull(found);
    }

    @Test
    @DisplayName("delete() should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        doNothing().when(patientRepository).deleteById(anyLong());

        patientService.delete(1L);

        verify(patientRepository, times(1)).deleteById(1L);
    }
}
