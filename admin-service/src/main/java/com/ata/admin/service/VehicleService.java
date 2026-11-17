package com.ata.admin.service;

import com.ata.admin.dto.VehicleDto;
import com.ata.admin.entity.Vehicle;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle addVehicle(VehicleDto dto) {
        logger.info("Adding vehicle: {}", dto.getVehicleNumber());
        if (vehicleRepository.existsByVehicleNumber(dto.getVehicleNumber())) {
            throw new DuplicateResourceException("Vehicle number already exists: " + dto.getVehicleNumber());
        }
        Vehicle vehicle = new Vehicle();
        mapDtoToEntity(dto, vehicle);
        Vehicle saved = vehicleRepository.save(vehicle);
        logger.info("Vehicle added with ID: {}", saved.getVehicleId());
        return saved;
    }

    public Vehicle updateVehicle(Long id, VehicleDto dto) {
        logger.info("Updating vehicle ID: {}", id);
        Vehicle vehicle = getVehicleById(id);
        mapDtoToEntity(dto, vehicle);
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        logger.info("Deleting vehicle ID: {}", id);
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
    }

    public List<Vehicle> getAllVehicles() {
        logger.debug("Fetching all vehicles");
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
    }

    private void mapDtoToEntity(VehicleDto dto, Vehicle vehicle) {
        vehicle.setVehicleNumber(dto.getVehicleNumber());
        vehicle.setVehicleName(dto.getVehicleName());
        vehicle.setVehicleType(dto.getVehicleType());
        vehicle.setSeatingCapacity(dto.getSeatingCapacity());
        vehicle.setFarePerKm(dto.getFarePerKm());
        vehicle.setDriverId(dto.getDriverId());
        if (dto.getStatus() != null) {
            try {
                vehicle.setStatus(Vehicle.VehicleStatus.valueOf(dto.getStatus().toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                throw new com.ata.admin.exception.ValidationException(
                    "Invalid vehicle status: '" + dto.getStatus() + "'. Must be AVAILABLE, BOOKED, or MAINTENANCE");
            }
        }
    }
}
