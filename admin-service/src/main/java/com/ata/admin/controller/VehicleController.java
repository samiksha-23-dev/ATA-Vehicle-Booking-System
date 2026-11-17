package com.ata.admin.controller;

import com.ata.admin.dto.ApiResponse;
import com.ata.admin.dto.VehicleDto;
import com.ata.admin.entity.Vehicle;
import com.ata.admin.service.VehicleService;
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
@RequestMapping("/api/admin/vehicles")
@Tag(name = "Vehicle Management", description = "CRUD operations for vehicles")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new vehicle")
    public ResponseEntity<ApiResponse<Vehicle>> addVehicle(@Valid @RequestBody VehicleDto dto) {
        logger.info("POST /api/admin/vehicles");
        Vehicle vehicle = vehicleService.addVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Vehicle added successfully", vehicle));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vehicle by ID")
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(
            @Parameter(description = "Vehicle ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody VehicleDto dto) {
        logger.info("PUT /api/admin/vehicles/{}", id);
        Vehicle vehicle = vehicleService.updateVehicle(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicle));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete vehicle by ID")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @Parameter(description = "Vehicle ID", example = "1") @PathVariable("id") Long id) {
        logger.info("DELETE /api/admin/vehicles/{}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicleById(
            @Parameter(description = "Vehicle ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/admin/vehicles/{}", id);
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle fetched", vehicle));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get all vehicles")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAllVehicles() {
        logger.info("GET /api/admin/vehicles");
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(ApiResponse.success("Vehicles fetched", vehicles));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get all available vehicles")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAvailableVehicles() {
        logger.info("GET /api/admin/vehicles/available");
        List<Vehicle> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(ApiResponse.success("Available vehicles fetched", vehicles));
    }
}
