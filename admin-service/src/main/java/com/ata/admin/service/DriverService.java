package com.ata.admin.service;

import com.ata.admin.dto.DriverDto;
import com.ata.admin.entity.Driver;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver addDriver(DriverDto dto) {
        logger.info("Adding driver with license: {}", dto.getLicenseNumber());
        if (driverRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("License number already exists: " + dto.getLicenseNumber());
        }
        if (driverRepository.existsByPhone(dto.getPhone())) {
            throw new DuplicateResourceException("Phone number already exists: " + dto.getPhone());
        }
        Driver driver = new Driver();
        mapDtoToEntity(dto, driver);
        Driver saved = driverRepository.save(driver);
        logger.info("Driver added with ID: {}", saved.getDriverId());
        return saved;
    }

    public Driver updateDriver(Long id, DriverDto dto) {
        logger.info("Updating driver ID: {}", id);
        Driver driver = getDriverById(id);
        mapDtoToEntity(dto, driver);
        return driverRepository.save(driver);
    }

    public void deleteDriver(Long id) {
        logger.info("Deleting driver ID: {}", id);
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver not found with ID: " + id);
        }
        driverRepository.deleteById(id);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));
    }

    public List<Driver> getAllDrivers() {
        logger.debug("Fetching all drivers");
        return driverRepository.findAll();
    }

    private void mapDtoToEntity(DriverDto dto, Driver driver) {
        driver.setName(dto.getName());
        driver.setAddress(dto.getAddress());
        driver.setPhone(dto.getPhone());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setExperience(dto.getExperience());
        if (dto.getAvailabilityStatus() != null) {
            try {
                driver.setAvailabilityStatus(
                    Driver.AvailabilityStatus.valueOf(dto.getAvailabilityStatus().toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                throw new com.ata.admin.exception.ValidationException(
                    "Invalid availability status: '" + dto.getAvailabilityStatus() + "'. Must be AVAILABLE, BUSY, or INACTIVE");
            }
        }
    }
}
