package com.ata.booking.controller;

import com.ata.booking.dto.ApiResponse;
import com.ata.booking.dto.BookingDtos.BookingRequest;
import com.ata.booking.dto.BookingDtos.SearchRequest;
import com.ata.booking.dto.BookingDtos.SearchResult;
import com.ata.booking.entity.Booking;
import com.ata.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Booking Management", description = "Search, book, cancel and view bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Search available vehicles for a route and date")
    public ResponseEntity<ApiResponse<List<SearchResult>>> searchVehicles(@RequestBody SearchRequest request) {
        logger.info("POST /api/booking/search");
        List<SearchResult> results = bookingService.searchVehicles(request);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Check vehicle availability for a date")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @Parameter(description = "Vehicle ID", example = "1") @RequestParam("vehicleId") Long vehicleId,
            @Parameter(description = "Journey date (yyyy-MM-dd)", example = "2026-12-25") @RequestParam("journeyDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
            @Parameter(description = "Number of passengers", example = "2") @RequestParam("passengerCount") int passengerCount) {
        logger.info("GET /api/booking/availability - vehicle: {}, date: {}", vehicleId, journeyDate);
        boolean available = bookingService.checkAvailability(vehicleId, journeyDate, passengerCount);
        return ResponseEntity.ok(ApiResponse.success("Availability checked", available));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<Booking>> createBooking(@Valid @RequestBody BookingRequest request) {
        logger.info("POST /api/booking - customer: {}", request.getCustomerId());
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Booking created successfully", booking));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Cancel a booking by ID")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(
            @Parameter(description = "Booking ID", example = "1") @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        logger.info("PUT /api/booking/{}/cancel by {}", id, userEmail);
        Booking booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "View booking details by ID")
    public ResponseEntity<ApiResponse<Booking>> viewBooking(
            @Parameter(description = "Booking ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/booking/{}", id);
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched", booking));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "View booking status by ID")
    public ResponseEntity<ApiResponse<Booking.BookingStatus>> viewBookingStatus(
            @Parameter(description = "Booking ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/booking/{}/status", id);
        Booking.BookingStatus status = bookingService.viewBookingStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Booking status", status));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update booking status (Admin only)")
    public ResponseEntity<ApiResponse<Booking>> updateStatus(
            @Parameter(description = "Booking ID", example = "1") @PathVariable("id") Long id,
            @Parameter(description = "New status", example = "COMPLETED") @RequestParam("status") String status) {
        logger.info("PUT /api/booking/{}/status?status={}", id, status);
        Booking booking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Booking status updated", booking));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get bookings for a customer (paginated)")
    public ResponseEntity<ApiResponse<Page<Booking>>> getBookingsByCustomer(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("customerId") Long customerId,
            Pageable pageable) {
        logger.info("GET /api/booking/customer/{}", customerId);
        Page<Booking> bookings = bookingService.getBookingsByCustomerPaged(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customer bookings fetched", bookings));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (Admin only, paginated)")
    public ResponseEntity<ApiResponse<Page<Booking>>> getAllBookings(Pageable pageable) {
        logger.info("GET /api/booking");
        Page<Booking> bookings = bookingService.getAllBookingsPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success("All bookings fetched", bookings));
    }
}
