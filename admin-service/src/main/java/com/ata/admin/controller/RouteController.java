package com.ata.admin.controller;

import com.ata.admin.dto.ApiResponse;
import com.ata.admin.dto.RouteDto;
import com.ata.admin.entity.Route;
import com.ata.admin.service.RouteService;
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
@RequestMapping("/api/admin/routes")
@Tag(name = "Route Management", description = "CRUD operations for routes")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new route")
    public ResponseEntity<ApiResponse<Route>> addRoute(@Valid @RequestBody RouteDto dto) {
        logger.info("POST /api/admin/routes");
        Route route = routeService.addRoute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Route added successfully", route));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update route by ID")
    public ResponseEntity<ApiResponse<Route>> updateRoute(
            @Parameter(description = "Route ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody RouteDto dto) {
        logger.info("PUT /api/admin/routes/{}", id);
        Route route = routeService.updateRoute(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Route updated successfully", route));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete route by ID")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @Parameter(description = "Route ID", example = "1") @PathVariable("id") Long id) {
        logger.info("DELETE /api/admin/routes/{}", id);
        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<ApiResponse<Route>> getRouteById(
            @Parameter(description = "Route ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/admin/routes/{}", id);
        Route route = routeService.getRouteById(id);
        return ResponseEntity.ok(ApiResponse.success("Route fetched", route));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get all routes")
    public ResponseEntity<ApiResponse<List<Route>>> getAllRoutes() {
        logger.info("GET /api/admin/routes");
        List<Route> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(ApiResponse.success("Routes fetched", routes));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Search routes by source and destination")
    public ResponseEntity<ApiResponse<List<Route>>> searchRoutes(
            @Parameter(description = "Source city", example = "Mumbai") @RequestParam("source") String source,
            @Parameter(description = "Destination city", example = "Pune") @RequestParam("destination") String destination) {
        logger.info("GET /api/admin/routes/search?source={}&destination={}", source, destination);
        List<Route> routes = routeService.searchRoutes(source, destination);
        return ResponseEntity.ok(ApiResponse.success("Routes found", routes));
    }
}
