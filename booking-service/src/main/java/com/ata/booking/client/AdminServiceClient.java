package com.ata.booking.client;

import com.ata.booking.dto.ApiResponse;
import com.ata.booking.dto.BookingDtos;
import com.ata.booking.dto.BookingDtos.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "admin-service", path = "/api/admin")
public interface AdminServiceClient {

    @GetMapping("/vehicles/{id}")
    ApiResponse<VehicleDto> getVehicleById(@PathVariable("id") Long id);

    @GetMapping("/vehicles/available")
    ApiResponse<List<VehicleDto>> getAvailableVehicles();

    @GetMapping("/vehicles")
    ApiResponse<List<VehicleDto>> getAllVehicles();

    @PutMapping("/vehicles/{id}")
    ApiResponse<VehicleDto> updateVehicle(@PathVariable("id") Long id, @RequestBody VehicleDto dto);

    @GetMapping("/routes/{id}")
    ApiResponse<BookingDtos.RouteDto> getRouteById(@PathVariable("id") Long id);

    @GetMapping("/routes/search")
    ApiResponse<List<BookingDtos.RouteDto>> searchRoutes(
            @RequestParam("source") String source,
            @RequestParam("destination") String destination);
}
