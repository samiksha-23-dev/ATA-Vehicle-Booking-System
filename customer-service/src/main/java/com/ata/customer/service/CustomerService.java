package com.ata.customer.service;

import com.ata.customer.client.BookingServiceClient;
import com.ata.customer.dto.CustomerDtos.CustomerRequest;
import com.ata.customer.dto.CustomerDtos.CustomerResponse;
import com.ata.customer.dto.CustomerDtos.UpdateRequest;
import com.ata.customer.entity.Customer;
import com.ata.customer.exception.DuplicateResourceException;
import com.ata.customer.exception.ResourceNotFoundException;
import com.ata.customer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingServiceClient bookingServiceClient;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                           BookingServiceClient bookingServiceClient) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookingServiceClient = bookingServiceClient;
    }

    @Transactional
    public CustomerResponse addCustomer(CustomerRequest request) {
        log.info("Registering customer: {}", request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        Customer customer = new Customer();
        mapRequestToEntity(request, customer);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        Customer saved = customerRepository.save(customer);
        log.info("Customer registered with ID: {}", saved.getCustomerId());
        return toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateRequest request) {
        log.info("Updating customer ID: {}", id);
        Customer customer = findById(id);
        customer.setName(request.getName());
        customer.setDob(request.getDob());
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress());
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer ID: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer ID: {}", id);
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        log.debug("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Object> getBookingsForCustomer(Long customerId) {
        log.debug("Fetching bookings for customer: {}", customerId);
        findById(customerId); // validate customer exists
        var response = bookingServiceClient.getBookingsByCustomer(customerId);
        return response.getData() != null ? response.getData() : List.of();
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    private void mapRequestToEntity(CustomerRequest request, Customer customer) {
        customer.setName(request.getName());
        customer.setDob(request.getDob());
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getDob(),
                customer.getGender(),
                customer.getAddress(),
                customer.getPhone(),
                customer.getEmail(),
                customer.isActive()
        );
    }
}
