package com.ata.admin;

import com.ata.admin.dto.CustomerDto;
import com.ata.admin.entity.Customer;
import com.ata.admin.exception.DuplicateResourceException;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.repository.CustomerRepository;
import com.ata.admin.service.CustomerService;
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

    private CustomerDto customerDto;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDto();
        customerDto.setName("Jane Doe");
        customerDto.setEmail("jane@example.com");
        customerDto.setPhone("9876543210");
        customerDto.setPassword("password123");
        customerDto.setGender("Female");
        customerDto.setDob(LocalDate.of(1995, 5, 10));
        customerDto.setAddress("Mumbai");

        customer = new Customer();
        customer.setName("Jane Doe");
        customer.setEmail("jane@example.com");
        customer.setPhone("9876543210");
        customer.setPassword("encodedPassword");
        customer.setGender("Female");
        customer.setDob(LocalDate.of(1995, 5, 10));
        customer.setAddress("Mumbai");
    }

    @Test
    void addCustomer_success() {
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.addCustomer(customerDto);

        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void addCustomer_duplicateEmail_throwsException() {
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.addCustomer(customerDto));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.updateCustomer(1L, customerDto);

        assertNotNull(result);
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_withNewPassword_encodesPassword() {
        customerDto.setPassword("newPassword123");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncoded");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.updateCustomer(1L, customerDto);

        assertNotNull(result);
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void updateCustomer_withBlankPassword_doesNotEncode() {
        customerDto.setPassword("   ");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.updateCustomer(1L, customerDto);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateCustomer_notFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(99L, customerDto));
    }

    @Test
    void getCustomerById_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void getCustomerById_notFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
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
        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void getAllCustomers_returnsList() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
    }

    @Test
    void getAllCustomers_returnsEmpty() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> result = customerService.getAllCustomers();

        assertTrue(result.isEmpty());
    }
}
