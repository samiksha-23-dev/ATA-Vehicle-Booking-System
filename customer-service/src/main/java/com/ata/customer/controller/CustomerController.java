package com.ata.customer.controller;

import com.ata.customer.dto.ApiResponse;
import com.ata.customer.dto.CustomerDtos.CustomerRequest;
import com.ata.customer.dto.CustomerDtos.CustomerResponse;
import com.ata.customer.dto.CustomerDtos.UpdateRequest;
import com.ata.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "Customer registration, profile management and admin operations")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(@Valid @RequestBody CustomerRequest request) {
        log.info("POST /api/customers/register - email: {}", request.getEmail());
        CustomerResponse response = customerService.addCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer registered successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Update customer profile by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRequest request) {
        log.info("PUT /api/customers/{}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer by ID (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id) {
        log.info("DELETE /api/customers/{}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id) {
        log.info("GET /api/customers/{}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer fetched", response));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get customer profile by email")
    public ResponseEntity<ApiResponse<CustomerResponse>> getProfile(
            @Parameter(description = "Customer email", example = "priya@example.com") @RequestParam("email") String email) {
        log.info("GET /api/customers/profile - email: {}", email);
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Get booking history for a customer")
    public ResponseEntity<ApiResponse<List<Object>>> getCustomerBookings(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id) {
        log.info("GET /api/customers/{}/bookings", id);
        List<Object> bookings = customerService.getBookingsForCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched", bookings));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers (Admin only)")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        log.info("GET /api/customers");
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success("Customers fetched", customers));
    }
}
