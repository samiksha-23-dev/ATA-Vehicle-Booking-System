package com.ata.admin;

import com.ata.admin.dto.VehicleDto;
import com.ata.admin.entity.Vehicle;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.VehicleRepository;
import com.ata.admin.service.VehicleService;
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
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleDto vehicleDto;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleDto = new VehicleDto();
        vehicleDto.setVehicleNumber("MH01AB1234");
        vehicleDto.setVehicleName("Express Bus");
        vehicleDto.setVehicleType("BUS");
        vehicleDto.setSeatingCapacity(40);
        vehicleDto.setFarePerKm(2.5);

        vehicle = new Vehicle();
        vehicle.setVehicleNumber("MH01AB1234");
        vehicle.setVehicleName("Express Bus");
        vehicle.setVehicleType("BUS");
        vehicle.setSeatingCapacity(40);
        vehicle.setFarePerKm(2.5);
    }

    @Test
    void addVehicle_success() {
        when(vehicleRepository.existsByVehicleNumber("MH01AB1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = vehicleService.addVehicle(vehicleDto);

        assertNotNull(result);
        assertEquals("MH01AB1234", result.getVehicleNumber());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void addVehicle_withStatus_success() {
        vehicleDto.setStatus("BOOKED");
        when(vehicleRepository.existsByVehicleNumber("MH01AB1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = vehicleService.addVehicle(vehicleDto);

        assertNotNull(result);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void addVehicle_duplicateNumber_throwsException() {
        when(vehicleRepository.existsByVehicleNumber("MH01AB1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.addVehicle(vehicleDto));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void updateVehicle_success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = vehicleService.updateVehicle(1L, vehicleDto);

        assertNotNull(result);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void updateVehicle_notFound_throwsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.updateVehicle(99L, vehicleDto));
    }

    @Test
    void getVehicleById_success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getVehicleById(1L);

        assertNotNull(result);
        assertEquals("Express Bus", result.getVehicleName());
    }

    @Test
    void getVehicleById_notFound_throwsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(99L));
    }

    @Test
    void deleteVehicle_notFound_throwsException() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.deleteVehicle(99L));
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    void deleteVehicle_success() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        assertDoesNotThrow(() -> vehicleService.deleteVehicle(1L));
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void getAllVehicles_returnsList() {
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertEquals(1, result.size());
    }

    @Test
    void getAllVehicles_returnsEmpty() {
        when(vehicleRepository.findAll()).thenReturn(List.of());

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableVehicles_returnsAvailable() {
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(List.of(vehicle));

        List<Vehicle> result = vehicleService.getAvailableVehicles();

        assertEquals(1, result.size());
    }
}
