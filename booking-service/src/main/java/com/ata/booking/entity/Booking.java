package com.ata.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @NotNull
    private LocalDateTime bookingDate = LocalDateTime.now();

    @NotNull
    private LocalDate journeyDate;

    @NotNull
    private Long customerId;

    @NotNull
    private Long routeId;

    @NotNull
    private Long vehicleId;

    @NotNull
    private Long driverId;

    @NotNull
    private String boardingPoint;

    @NotNull
    private String dropPoint;

    @Min(1)
    private int passengerCount;

    private double fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    public enum BookingStatus {
        PENDING, CONFIRMED, ONGOING, COMPLETED, CANCELLED
    }

    public Booking() {}

    public Long getBookingId() { return bookingId; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getBoardingPoint() { return boardingPoint; }
    public void setBoardingPoint(String boardingPoint) { this.boardingPoint = boardingPoint; }
    public String getDropPoint() { return dropPoint; }
    public void setDropPoint(String dropPoint) { this.dropPoint = dropPoint; }
    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
