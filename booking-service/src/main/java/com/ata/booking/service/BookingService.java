package com.ata.booking.service;

import com.ata.booking.client.AdminServiceClient;
import com.ata.booking.dto.ApiResponse;
import com.ata.booking.dto.BookingDtos;
import com.ata.booking.dto.BookingDtos.BookingRequest;
import com.ata.booking.dto.BookingDtos.SearchRequest;
import com.ata.booking.dto.BookingDtos.SearchResult;
import com.ata.booking.dto.BookingDtos.VehicleDto;
import com.ata.booking.entity.Booking;
import com.ata.booking.exception.BookingException;
import com.ata.booking.exception.ResourceNotFoundException;
import com.ata.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final AdminServiceClient adminServiceClient;

    public BookingService(BookingRepository bookingRepository, AdminServiceClient adminServiceClient) {
        this.bookingRepository = bookingRepository;
        this.adminServiceClient = adminServiceClient;
    }

    @Transactional(readOnly = true)
    public List<SearchResult> searchVehicles(SearchRequest request) {
        logger.info("Searching vehicles: {} -> {} on {}", request.getSource(), request.getDestination(), request.getJourneyDate());

        ApiResponse<List<BookingDtos.RouteDto>> routeResponse = adminServiceClient.searchRoutes(
                request.getSource(), request.getDestination());

        if (routeResponse.getData() == null || routeResponse.getData().isEmpty()) {
            return new ArrayList<>();
        }

        List<VehicleDto> availableVehicles = getAvailableVehicleList();
        List<SearchResult> results = new ArrayList<>();

        for (BookingDtos.RouteDto route : routeResponse.getData()) {
            for (VehicleDto vehicle : availableVehicles) {
                addSearchResultIfEligible(request, route, vehicle, results);
            }
        }
        logger.info("Found {} search results", results.size());
        return results;
    }

    private List<VehicleDto> getAvailableVehicleList() {
        ApiResponse<List<VehicleDto>> vehicleResponse = adminServiceClient.getAvailableVehicles();
        return vehicleResponse.getData() != null ? vehicleResponse.getData() : new ArrayList<>();
    }

    private void addSearchResultIfEligible(SearchRequest request, BookingDtos.RouteDto route,
                                            VehicleDto vehicle, List<SearchResult> results) {
        if (request.getVehicleType() != null && !vehicle.getVehicleType().equalsIgnoreCase(request.getVehicleType())) {
            return;
        }
        double fare = route.getDistance() * vehicle.getFarePerKm();
        if (request.getMaxFare() != null && fare > request.getMaxFare()) {
            return;
        }
        if (request.getJourneyDate() != null) {
            int booked = bookingRepository.sumPassengersByVehicleAndDate(vehicle.getVehicleId(), request.getJourneyDate());
            if (booked >= vehicle.getSeatingCapacity()) {
                return;
            }
        }
        results.add(new SearchResult(route, vehicle, fare));
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(Long vehicleId, LocalDate journeyDate, int passengerCount) {
        logger.debug("Checking availability for vehicle {} on {}", vehicleId, journeyDate);
        ApiResponse<VehicleDto> response = adminServiceClient.getVehicleById(vehicleId);
        VehicleDto vehicle = response.getData();
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }
        int booked = bookingRepository.sumPassengersByVehicleAndDate(vehicleId, journeyDate);
        return (booked + passengerCount) <= vehicle.getSeatingCapacity();
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        logger.info("Creating booking for customer {} on route {}", request.getCustomerId(), request.getRouteId());

        BookingDtos.RouteDto route = fetchRoute(request.getRouteId());
        VehicleDto assignedVehicle = findAvailableVehicleWithDriver(request);

        double fare = route.getDistance() * assignedVehicle.getFarePerKm() * request.getPassengerCount();

        Booking booking = new Booking();
        booking.setCustomerId(request.getCustomerId());
        booking.setRouteId(request.getRouteId());
        booking.setVehicleId(assignedVehicle.getVehicleId());
        booking.setDriverId(assignedVehicle.getDriverId());
        booking.setJourneyDate(request.getJourneyDate());
        booking.setBoardingPoint(request.getBoardingPoint());
        booking.setDropPoint(request.getDropPoint());
        booking.setPassengerCount(request.getPassengerCount());
        booking.setFare(fare);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        logger.info("Booking created with ID: {}", saved.getBookingId());
        return saved;
    }

    private BookingDtos.RouteDto fetchRoute(Long routeId) {
        ApiResponse<BookingDtos.RouteDto> routeResponse = adminServiceClient.getRouteById(routeId);
        BookingDtos.RouteDto route = routeResponse.getData();
        if (route == null) {
            throw new ResourceNotFoundException("Route not found: " + routeId);
        }
        return route;
    }

    private VehicleDto findAvailableVehicleWithDriver(BookingRequest request) {
        List<VehicleDto> availableVehicles = getAvailableVehicleList();
        if (availableVehicles.isEmpty()) {
            throw new BookingException("No available vehicles for this route");
        }
        for (VehicleDto v : availableVehicles) {
            if (v.getDriverId() == null) {
                continue;
            }
            if (!checkAvailability(v.getVehicleId(), request.getJourneyDate(), request.getPassengerCount())) {
                continue;
            }
            List<Booking> driverBookings = bookingRepository.findActiveBookingsByDriverAndDate(
                    v.getDriverId(), request.getJourneyDate());
            if (driverBookings.isEmpty()) {
                return v;
            }
        }
        throw new BookingException("No available vehicle with driver for the requested journey date");
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        logger.info("Cancelling booking ID: {}", bookingId);
        Booking booking = getBookingById(bookingId);
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BookingException("Cannot cancel a completed booking");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
    }

    @Transactional(readOnly = true)
    public Booking.BookingStatus viewBookingStatus(Long bookingId) {
        return getBookingById(bookingId).getStatus();
    }

    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByCustomerPaged(Long customerId, Pageable pageable) {
        return bookingRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Booking> getAllBookingsPaged(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByCustomer(Long customerId) {
        logger.debug("Fetching bookings for customer: {}", customerId);
        return bookingRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, String status) {
        logger.info("Updating booking {} status to {}", bookingId, status);
        Booking booking = getBookingById(bookingId);
        try {
            booking.setStatus(Booking.BookingStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BookingException("Invalid status: " + status);
        }
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        logger.debug("Fetching all bookings");
        return bookingRepository.findAll();
    }
}
