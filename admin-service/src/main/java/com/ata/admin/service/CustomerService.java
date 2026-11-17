package com.ata.admin.service;

import com.ata.admin.dto.CustomerDto;
import com.ata.admin.entity.Customer;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer addCustomer(CustomerDto dto) {
        logger.info("Adding customer: {}", dto.getEmail());
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + dto.getEmail());
        }
        Customer customer = new Customer();
        mapDtoToEntity(dto, customer);
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        Customer saved = customerRepository.save(customer);
        logger.info("Customer added with ID: {}", saved.getCustomerId());
        return saved;
    }

    public Customer updateCustomer(Long id, CustomerDto dto) {
        logger.info("Updating customer ID: {}", id);
        Customer customer = getCustomerById(id);
        mapDtoToEntity(dto, customer);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        logger.info("Deleting customer ID: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    public List<Customer> getAllCustomers() {
        logger.debug("Fetching all customers");
        return customerRepository.findAll();
    }

    private void mapDtoToEntity(CustomerDto dto, Customer customer) {
        customer.setName(dto.getName());
        customer.setDob(dto.getDob());
        customer.setGender(dto.getGender());
        customer.setAddress(dto.getAddress());
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
    }
}
