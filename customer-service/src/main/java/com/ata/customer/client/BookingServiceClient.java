package com.ata.customer.client;

import com.ata.customer.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "booking-service", path = "/api/booking")
public interface BookingServiceClient {

    @GetMapping("/customer/{customerId}")
    ApiResponse<List<Object>> getBookingsByCustomer(@PathVariable("customerId") Long customerId);
}
            