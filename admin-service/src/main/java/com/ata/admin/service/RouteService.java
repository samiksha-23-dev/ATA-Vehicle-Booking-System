package com.ata.admin.service;

import com.ata.admin.dto.RouteDto;
import com.ata.admin.entity.Route;
import com.ata.admin.exception.ResourceNotFoundException;
import com.ata.admin.exception.ValidationException;
import com.ata.admin.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route addRoute(RouteDto dto) {
        logger.info("Adding route: {} -> {}", dto.getSource(), dto.getDestination());
        if (dto.getSource().equalsIgnoreCase(dto.getDestination())) {
            throw new ValidationException("Source and destination cannot be the same");
        }
        Route route = new Route();
        mapDtoToEntity(dto, route);
        Route saved = routeRepository.save(route);
        logger.info("Route added with ID: {}", saved.getRouteId());
        return saved;
    }

    public Route updateRoute(Long id, RouteDto dto) {
        logger.info("Updating route ID: {}", id);
        if (dto.getSource().equalsIgnoreCase(dto.getDestination())) {
            throw new ValidationException("Source and destination cannot be the same");
        }
        Route route = getRouteById(id);
        mapDtoToEntity(dto, route);
        return routeRepository.save(route);
    }

    public void deleteRoute(Long id) {
        logger.info("Deleting route ID: {}", id);
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found with ID: " + id);
        }
        routeRepository.deleteById(id);
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));
    }

    public List<Route> getAllRoutes() {
        logger.debug("Fetching all routes");
        return routeRepository.findAll();
    }

    public List<Route> searchRoutes(String source, String destination) {
        logger.debug("Searching routes: {} -> {}", source, destination);
        return routeRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination);
    }

    private void mapDtoToEntity(RouteDto dto, Route route) {
        route.setSource(dto.getSource());
        route.setDestination(dto.getDestination());
        route.setDistance(dto.getDistance());
        route.setDuration(dto.getDuration());
    }
}
