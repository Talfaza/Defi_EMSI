package com.example.defi.services;

import com.example.defi.entities.Clinic;
import com.example.defi.repositories.ClinicRepository;
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
class ClinicServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicService clinicService;

    private Clinic testClinic;

    @BeforeEach
    void setUp() {
        testClinic = new Clinic();
        testClinic.setId(1L);
        testClinic.setName("Test Clinic");
        testClinic.setEmail("clinic@test.com");
        testClinic.setWalletAddress("0xCl1n1c000000000000000000000000000000001");
    }

    @Test
    @DisplayName("findAll() should return all clinics")
    void findAll_shouldReturnAllClinics() {
        Clinic anotherClinic = new Clinic();
        anotherClinic.setId(2L);
        anotherClinic.setName("Another Clinic");

        when(clinicRepository.findAll()).thenReturn(Arrays.asList(testClinic, anotherClinic));

        List<Clinic> clinics = clinicService.findAll();

        assertEquals(2, clinics.size());
        verify(clinicRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll() should return empty list when no clinics")
    void findAll_shouldReturnEmptyList_whenNoClinics() {
        when(clinicRepository.findAll()).thenReturn(Arrays.asList());

        List<Clinic> clinics = clinicService.findAll();

        assertTrue(clinics.isEmpty());
    }

    @Test
    @DisplayName("save() should save and return clinic")
    void save_shouldSaveAndReturnClinic() {
        when(clinicRepository.save(any(Clinic.class))).thenReturn(testClinic);

        Clinic savedClinic = clinicService.save(testClinic);

        assertNotNull(savedClinic);
        assertEquals(testClinic.getId(), savedClinic.getId());
        assertEquals(testClinic.getName(), savedClinic.getName());
        verify(clinicRepository, times(1)).save(testClinic);
    }

    @Test
    @DisplayName("findById() should return clinic when found")
    void findById_shouldReturnClinic_whenFound() {
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(testClinic));

        Clinic found = clinicService.findById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Test Clinic", found.getName());
    }

    @Test
    @DisplayName("findById() should return null when not found")
    void findById_shouldReturnNull_whenNotFound() {
        when(clinicRepository.findById(anyLong())).thenReturn(Optional.empty());

        Clinic found = clinicService.findById(999L);

        assertNull(found);
    }

    @Test
    @DisplayName("delete() should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        doNothing().when(clinicRepository).deleteById(anyLong());

        clinicService.delete(1L);

        verify(clinicRepository, times(1)).deleteById(1L);
    }
}
