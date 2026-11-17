package com.ata.admin;

import com.ata.admin.dto.DriverDto;
import com.ata.admin.entity.Driver;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.DriverRepository;
import com.ata.admin.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private DriverDto driverDto;
    private Driver driver;

    @BeforeEach
    void setUp() {
        driverDto = new DriverDto();
        driverDto.setName("John Doe");
        driverDto.setAddress("Mumbai");
        driverDto.setPhone("9876543210");
        driverDto.setLicenseNumber("MH1234567");
        driverDto.setExperience(5);

        driver = new Driver();
        driver.setName("John Doe");
        driver.setPhone("9876543210");
        driver.setLicenseNumber("MH1234567");
    }

    @Test
    void addDriver_success() {
        when(driverRepository.existsByLicenseNumber("MH1234567")).thenReturn(false);
        when(driverRepository.existsByPhone("9876543210")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        Driver result = driverService.addDriver(driverDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void addDriver_withAvailabilityStatus_success() {
        driverDto.setAvailabilityStatus("BUSY");
        when(driverRepository.existsByLicenseNumber("MH1234567")).thenReturn(false);
        when(driverRepository.existsByPhone("9876543210")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        Driver result = driverService.addDriver(driverDto);

        assertNotNull(result);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void addDriver_duplicateLicense_throwsException() {
        when(driverRepository.existsByLicenseNumber("MH1234567")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.addDriver(driverDto));
        verify(driverRepository, never()).save(any());
    }

    @Test
    void addDriver_duplicatePhone_throwsException() {
        when(driverRepository.existsByLicenseNumber("MH1234567")).thenReturn(false);
        when(driverRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.addDriver(driverDto));
        verify(driverRepository, never()).save(any());
    }

    @Test
    void updateDriver_success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        Driver result = driverService.updateDriver(1L, driverDto);

        assertNotNull(result);
        verify(driverRepository).save(driver);
    }

    @Test
    void updateDriver_notFound_throwsException() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.updateDriver(99L, driverDto));
    }

    @Test
    void getDriverById_success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        Driver result = driverService.getDriverById(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getDriverById_notFound_throwsException() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById(99L));
    }

    @Test
    void deleteDriver_success() {
        when(driverRepository.existsById(1L)).thenReturn(true);
        doNothing().when(driverRepository).deleteById(1L);

        assertDoesNotThrow(() -> driverService.deleteDriver(1L));
        verify(driverRepository).deleteById(1L);
    }

    @Test
    void deleteDriver_notFound_throwsException() {
        when(driverRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> driverService.deleteDriver(99L));
        verify(driverRepository, never()).deleteById(any());
    }

    @Test
    void getAllDrivers_returnsList() {
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        List<Driver> result = driverService.getAllDrivers();

        assertEquals(1, result.size());
    }

    @Test
    void getAllDrivers_returnsEmpty() {
        when(driverRepository.findAll()).thenReturn(List.of());

        List<Driver> result = driverService.getAllDrivers();

        assertTrue(result.isEmpty());
    }
}
