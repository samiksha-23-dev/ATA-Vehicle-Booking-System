package com.ata.admin;

import com.ata.admin.dto.RouteDto;
import com.ata.admin.entity.Route;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.exception.ValidationException;
import com.ata.admin.repository.RouteRepository;
import com.ata.admin.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    private RouteDto routeDto;
    private Route route;

    @BeforeEach
    void setUp() {
        routeDto = new RouteDto();
        routeDto.setSource("Mumbai");
        routeDto.setDestination("Pune");
        routeDto.setDistance(150.0);
        routeDto.setDuration("3h");

        route = new Route();
        route.setSource("Mumbai");
        route.setDestination("Pune");
        route.setDistance(150.0);
        route.setDuration("3h");
    }

    @Test
    void addRoute_success() {
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.addRoute(routeDto);

        assertNotNull(result);
        assertEquals("Mumbai", result.getSource());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void addRoute_sameSourceDestination_throwsException() {
        routeDto.setDestination("Mumbai");

        assertThrows(ValidationException.class, () -> routeService.addRoute(routeDto));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void updateRoute_success() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.updateRoute(1L, routeDto);

        assertNotNull(result);
        verify(routeRepository).save(route);
    }

    @Test
    void updateRoute_sameSourceDestination_throwsException() {
        routeDto.setDestination("Mumbai");

        assertThrows(ValidationException.class, () -> routeService.updateRoute(1L, routeDto));
        verify(routeRepository, never()).findById(any());
    }

    @Test
    void updateRoute_notFound_throwsException() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.updateRoute(99L, routeDto));
    }

    @Test
    void getRouteById_success() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        Route result = routeService.getRouteById(1L);

        assertNotNull(result);
        assertEquals("Pune", result.getDestination());
    }

    @Test
    void getRouteById_notFound_throwsException() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.getRouteById(99L));
    }

    @Test
    void deleteRoute_success() {
        when(routeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(routeRepository).deleteById(1L);

        assertDoesNotThrow(() -> routeService.deleteRoute(1L));
        verify(routeRepository).deleteById(1L);
    }

    @Test
    void deleteRoute_notFound_throwsException() {
        when(routeRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> routeService.deleteRoute(99L));
        verify(routeRepository, never()).deleteById(any());
    }

    @Test
    void getAllRoutes_returnsList() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<Route> result = routeService.getAllRoutes();

        assertEquals(1, result.size());
    }

    @Test
    void getAllRoutes_returnsEmpty() {
        when(routeRepository.findAll()).thenReturn(List.of());

        List<Route> result = routeService.getAllRoutes();

        assertTrue(result.isEmpty());
    }

    @Test
    void searchRoutes_returnsResults() {
        when(routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase("Mumbai", "Pune"))
                .thenReturn(List.of(route));

        List<Route> result = routeService.searchRoutes("Mumbai", "Pune");

        assertEquals(1, result.size());
    }

    @Test
    void searchRoutes_noResults() {
        when(routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase("Delhi", "Chennai"))
                .thenReturn(List.of());

        List<Route> result = routeService.searchRoutes("Delhi", "Chennai");

        assertTrue(result.isEmpty());
    }
}
