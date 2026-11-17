package com.ata.admin.controller;

import com.ata.admin.dto.ApiResponse;
import com.ata.admin.dto.CustomerDto;
import com.ata.admin.entity.Customer;
import com.ata.admin.service.CustomerService;
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
@RequestMapping("/api/admin/customers")
@Tag(name = "Customer Management", description = "CRUD operations for customers")
public class CustomerController {

    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<ApiResponse<Customer>> addCustomer(@Valid @RequestBody CustomerDto dto) {
        logger.info("POST /api/admin/customers");
        Customer customer = customerService.addCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer registered successfully", customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Update customer by ID")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody CustomerDto dto) {
        logger.info("PUT /api/admin/customers/{}", id);
        Customer customer = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id) {
        logger.info("DELETE /api/admin/customers/{}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")  
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(
            @Parameter(description = "Customer ID", example = "1") @PathVariable("id") Long id) {
        logger.info("GET /api/admin/customers/{}", id);
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer fetched", customer));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        logger.info("GET /api/admin/customers");
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success("Customers fetched", customers));
    }
}
