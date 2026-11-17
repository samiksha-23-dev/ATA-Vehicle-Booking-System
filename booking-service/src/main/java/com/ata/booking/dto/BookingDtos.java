package com.ata.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BookingDtos {

    public static class BookingRequest {
        @NotNull
        private Long customerId;

        @NotNull
        private Long routeId;

        @NotNull(message = "Journey date is required")
        @Future(message = "Journey date must be a future date")
        private LocalDate journeyDate;

        @NotBlank
        private String boardingPoint;

        @NotBlank
        private String dropPoint;

        @Min(value = 1, message = "Passenger count must be at least 1")
        private int passengerCount;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }
        public LocalDate getJourneyDate() { return journeyDate; }
        public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
        public String getBoardingPoint() { return boardingPoint; }
        public void setBoardingPoint(String boardingPoint) { this.boardingPoint = boardingPoint; }
        public String getDropPoint() { return dropPoint; }
        public void setDropPoint(String dropPoint) { this.dropPoint = dropPoint; }
        public int getPassengerCount() { return passengerCount; }
        public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }
    }

    public static class SearchRequest {
        private String source;
        private String destination;
        private LocalDate journeyDate;
        private String vehicleType;
        private Double maxFare;

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public LocalDate getJourneyDate() { return journeyDate; }
        public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public Double getMaxFare() { return maxFare; }
        public void setMaxFare(Double maxFare) { this.maxFare = maxFare; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleDto {
        private Long vehicleId;
        private String vehicleNumber;
        private String vehicleName;
        private String vehicleType;
        private int seatingCapacity;
        private double farePerKm;
        private String status;
        private Long driverId;

        public Long getVehicleId() { return vehicleId; }
        public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
        public String getVehicleNumber() { return vehicleNumber; }
        public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
        public String getVehicleName() { return vehicleName; }
        public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public int getSeatingCapacity() { return seatingCapacity; }
        public void setSeatingCapacity(int seatingCapacity) { this.seatingCapacity = seatingCapacity; }
        public double getFarePerKm() { return farePerKm; }
        public void setFarePerKm(double farePerKm) { this.farePerKm = farePerKm; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RouteDto {
        private Long routeId;
        private String source;
        private String destination;
        private double distance;
        private String duration;

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchResult {
        private RouteDto route;
        private VehicleDto vehicle;
        private double estimatedFare;

        public SearchResult() {}

        public SearchResult(RouteDto route, VehicleDto vehicle, double estimatedFare) {
            this.route = route;
            this.vehicle = vehicle;
            this.estimatedFare = estimatedFare;
        }

        public RouteDto getRoute() { return route; }
        public void setRoute(RouteDto route) { this.route = route; }
        public VehicleDto getVehicle() { return vehicle; }
        public void setVehicle(VehicleDto vehicle) { this.vehicle = vehicle; }
        public double getEstimatedFare() { return estimatedFare; }
        public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }
    }
}
