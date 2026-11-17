package com.ata.admin.controller;

import com.ata.admin.dto.ApiResponse;
import com.ata.admin.dto.DriverDto;
import com.ata.admin.entity.Driver;
import com.ata.admin.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/drivers")
@Tag(name = "Driver Management", description = "CRUD operations for drivers")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new driver")
    public ResponseEntity<ApiResponse<Driver>> addDriver(@Valid @RequestBody DriverDto dto) {
        logger.info("POST /api/admin/drivers");
        Driver driver = driverService.addDriver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Driver added successfully", driver));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update driver by ID")
    public ResponseEntity<ApiResponse<Driver>> updateDriver(
            @Parameter(description = "Driver ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody DriverDto dto) {
        logger.info("PUT /api/admin/drivers/{}", id);
        Driver driver = driverService.updateDriver(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Driver updated successfully", driver));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete driver by ID")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(
            @Parameter(description = "Driver ID", example = "1") @PathVariable("id") Long id) {
        logger.info("DELETE /api/admin/drivers/{}", id);
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Driver deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<ApiResponse<Driver>> getDriverById(
            @Parameter(description = "Driver ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/admin/drivers/{}", id);
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success("Driver fetched", driver));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get all drivers")
    public ResponseEntity<ApiResponse<List<Driver>>> getAllDrivers() {
        logger.info("GET /api/admin/drivers");
        List<Driver> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(ApiResponse.success("Drivers fetched", drivers));
    }
}
