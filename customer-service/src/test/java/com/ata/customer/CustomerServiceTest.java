package com.ata.customer;

import com.ata.customer.dto.CustomerDtos.CustomerRequest;
import com.ata.customer.dto.CustomerDtos.CustomerResponse;
import com.ata.customer.dto.CustomerDtos.UpdateRequest;
import com.ata.customer.entity.Customer;
import com.ata.customer.exception.DuplicateResourceException;
import com.ata.customer.exception.ResourceNotFoundException;
import com.ata.customer.repository.CustomerRepository;
import com.ata.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("Jane Doe");
        customer.setEmail("jane@example.com");
        customer.setPhone("9876543210");
        customer.setPassword("encodedPassword");
        customer.setGender("Female");
        customer.setDob(LocalDate.of(1995, 5, 10));
        customer.setAddress("Mumbai");
        customer.setActive(true);
    }

    @Test
    void addCustomer_success() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPhone("9876543210");
        request.setPassword("password123");
        request.setGender("Female");
        request.setDob(LocalDate.of(1995, 5, 10));
        request.setAddress("Mumbai");

        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.addCustomer(request);

        assertNotNull(response);
        assertEquals("jane@example.com", response.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void addCustomer_duplicateEmail_throwsException() {
        CustomerRequest request = new CustomerRequest();
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.addCustomer(request));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_success() {
        UpdateRequest request = new UpdateRequest();
        request.setName("Jane Updated");
        request.setPhone("9876543210");
        request.setGender("Female");
        request.setDob(LocalDate.of(1995, 5, 10));
        request.setAddress("Pune");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.updateCustomer(1L, request);

        assertNotNull(response);
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_notFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateCustomer(99L, new UpdateRequest()));
    }

    @Test
    void deleteCustomer_success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_notFound_throwsException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(99L));
    }

    @Test
    void getCustomerById_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void getCustomerById_notFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    void getCustomerByEmail_success() {
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerByEmail("jane@example.com");

        assertNotNull(response);
        assertEquals("Jane Doe", response.getName());
    }

    @Test
    void getCustomerByEmail_notFound_throwsException() {
        when(customerRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerByEmail("notfound@example.com"));
    }

    @Test
    void getAllCustomers_returnsList() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("jane@example.com", result.get(0).getEmail());
    }
}
