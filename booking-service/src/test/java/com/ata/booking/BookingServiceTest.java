package com.ata.booking;

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
import com.ata.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AdminServiceClient adminServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private VehicleDto vehicleDto;
    private BookingDtos.RouteDto routeDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        vehicleDto = new VehicleDto();
        vehicleDto.setVehicleId(1L);
        vehicleDto.setVehicleName("Bus A");
        vehicleDto.setVehicleType("BUS");
        vehicleDto.setSeatingCapacity(40);
        vehicleDto.setFarePerKm(2.5);
        vehicleDto.setStatus("AVAILABLE");
        vehicleDto.setDriverId(10L);

        routeDto = new BookingDtos.RouteDto();
        routeDto.setRouteId(1L);
        routeDto.setSource("Mumbai");
        routeDto.setDestination("Pune");
        routeDto.setDistance(150.0);
        routeDto.setDuration("3h");

        booking = new Booking();
        booking.setCustomerId(1L);
        booking.setRouteId(1L);
        booking.setVehicleId(1L);
        booking.setDriverId(10L);
        booking.setJourneyDate(LocalDate.now().plusDays(1));
        booking.setBoardingPoint("Mumbai Central");
        booking.setDropPoint("Pune Station");
        booking.setPassengerCount(2);
        booking.setFare(375.0);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
    }

    @Test
    void searchVehicles_returnsResults() {
        SearchRequest request = new SearchRequest();
        request.setSource("Mumbai");
        request.setDestination("Pune");
        request.setJourneyDate(LocalDate.now().plusDays(1));

        when(adminServiceClient.searchRoutes("Mumbai", "Pune"))
                .thenReturn(ApiResponse.success("ok", List.of(routeDto)));
        when(adminServiceClient.getAvailableVehicles())
                .thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, request.getJourneyDate())).thenReturn(0);

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void searchVehicles_noRoutes_returnsEmpty() {
        SearchRequest request = new SearchRequest();
        request.setSource("Delhi");
        request.setDestination("Chennai");

        when(adminServiceClient.searchRoutes("Delhi", "Chennai"))
                .thenReturn(ApiResponse.success("ok", List.of()));

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchVehicles_nullRouteData_returnsEmpty() {
        SearchRequest request = new SearchRequest();
        request.setSource("Delhi");
        request.setDestination("Chennai");

        when(adminServiceClient.searchRoutes("Delhi", "Chennai"))
                .thenReturn(ApiResponse.success("ok", null));

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchVehicles_withVehicleTypeFilter_filtersCorrectly() {
        SearchRequest request = new SearchRequest();
        request.setSource("Mumbai");
        request.setDestination("Pune");
        request.setVehicleType("CAR");

        when(adminServiceClient.searchRoutes("Mumbai", "Pune"))
                .thenReturn(ApiResponse.success("ok", List.of(routeDto)));
        when(adminServiceClient.getAvailableVehicles())
                .thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchVehicles_withMaxFareFilter_filtersHighFare() {
        SearchRequest request = new SearchRequest();
        request.setSource("Mumbai");
        request.setDestination("Pune");
        request.setMaxFare(100.0);

        when(adminServiceClient.searchRoutes("Mumbai", "Pune"))
                .thenReturn(ApiResponse.success("ok", List.of(routeDto)));
        when(adminServiceClient.getAvailableVehicles())
                .thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchVehicles_vehicleFullyBooked_excluded() {
        SearchRequest request = new SearchRequest();
        request.setSource("Mumbai");
        request.setDestination("Pune");
        request.setJourneyDate(LocalDate.now().plusDays(1));

        when(adminServiceClient.searchRoutes("Mumbai", "Pune"))
                .thenReturn(ApiResponse.success("ok", List.of(routeDto)));
        when(adminServiceClient.getAvailableVehicles())
                .thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, request.getJourneyDate())).thenReturn(40);

        List<SearchResult> results = bookingService.searchVehicles(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void createBooking_success() {
        BookingRequest request = new BookingRequest();
        request.setCustomerId(1L);
        request.setRouteId(1L);
        request.setJourneyDate(LocalDate.now().plusDays(1));
        request.setBoardingPoint("Mumbai Central");
        request.setDropPoint("Pune Station");
        request.setPassengerCount(2);

        when(adminServiceClient.getRouteById(1L)).thenReturn(ApiResponse.success("ok", routeDto));
        when(adminServiceClient.getAvailableVehicles()).thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));
        when(adminServiceClient.getVehicleById(1L)).thenReturn(ApiResponse.success("ok", vehicleDto));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, request.getJourneyDate())).thenReturn(0);
        when(bookingRepository.findActiveBookingsByDriverAndDate(10L, request.getJourneyDate())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.createBooking(request);

        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_routeNotFound_throwsException() {
        BookingRequest request = new BookingRequest();
        request.setRouteId(99L);
        request.setJourneyDate(LocalDate.now().plusDays(1));

        when(adminServiceClient.getRouteById(99L)).thenReturn(ApiResponse.success("ok", null));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void createBooking_noAvailableVehicle_throwsException() {
        BookingRequest request = new BookingRequest();
        request.setCustomerId(1L);
        request.setRouteId(1L);
        request.setJourneyDate(LocalDate.now().plusDays(1));
        request.setPassengerCount(2);

        when(adminServiceClient.getRouteById(1L)).thenReturn(ApiResponse.success("ok", routeDto));
        when(adminServiceClient.getAvailableVehicles()).thenReturn(ApiResponse.success("ok", List.of()));

        assertThrows(BookingException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void createBooking_vehicleWithNoDriver_skipped_throwsException() {
        vehicleDto.setDriverId(null);
        BookingRequest request = new BookingRequest();
        request.setCustomerId(1L);
        request.setRouteId(1L);
        request.setJourneyDate(LocalDate.now().plusDays(1));
        request.setPassengerCount(2);

        when(adminServiceClient.getRouteById(1L)).thenReturn(ApiResponse.success("ok", routeDto));
        when(adminServiceClient.getAvailableVehicles()).thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));

        assertThrows(BookingException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void createBooking_driverAlreadyBooked_throwsException() {
        BookingRequest request = new BookingRequest();
        request.setCustomerId(1L);
        request.setRouteId(1L);
        request.setJourneyDate(LocalDate.now().plusDays(1));
        request.setPassengerCount(2);

        when(adminServiceClient.getRouteById(1L)).thenReturn(ApiResponse.success("ok", routeDto));
        when(adminServiceClient.getAvailableVehicles()).thenReturn(ApiResponse.success("ok", List.of(vehicleDto)));
        when(adminServiceClient.getVehicleById(1L)).thenReturn(ApiResponse.success("ok", vehicleDto));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, request.getJourneyDate())).thenReturn(0);
        when(bookingRepository.findActiveBookingsByDriverAndDate(10L, request.getJourneyDate()))
                .thenReturn(List.of(booking));

        assertThrows(BookingException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void cancelBooking_success() {
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancelBooking(1L);

        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelBooking_alreadyCancelled_throwsException() {
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void cancelBooking_completed_throwsException() {
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void getBookingById_notFound_throwsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(99L));
    }

    @Test
    void viewBookingStatus_returnsStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking.BookingStatus status = bookingService.viewBookingStatus(1L);

        assertEquals(Booking.BookingStatus.CONFIRMED, status);
    }

    @Test
    void checkAvailability_available_returnsTrue() {
        when(adminServiceClient.getVehicleById(1L)).thenReturn(ApiResponse.success("ok", vehicleDto));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, LocalDate.now().plusDays(1))).thenReturn(10);

        boolean result = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), 5);

        assertTrue(result);
    }

    @Test
    void checkAvailability_full_returnsFalse() {
        when(adminServiceClient.getVehicleById(1L)).thenReturn(ApiResponse.success("ok", vehicleDto));
        when(bookingRepository.sumPassengersByVehicleAndDate(1L, LocalDate.now().plusDays(1))).thenReturn(38);

        boolean result = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), 5);

        assertFalse(result);
    }

    @Test
    void checkAvailability_vehicleNotFound_throwsException() {
        when(adminServiceClient.getVehicleById(99L)).thenReturn(ApiResponse.success("ok", null));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.checkAvailability(99L, LocalDate.now().plusDays(1), 2));
    }

    @Test
    void getAllBookings_returnsList() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllBookings();

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByCustomer_returnsList() {
        when(bookingRepository.findByCustomerId(1L)).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getBookingsByCustomer(1L);

        assertEquals(1, result.size());
    }
}
