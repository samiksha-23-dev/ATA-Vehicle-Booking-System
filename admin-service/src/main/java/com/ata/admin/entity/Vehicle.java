package com.ata.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @NotBlank
    @Column(nullable = false)
    private String vehicleName;

    @NotBlank
    private String vehicleType;

    @Positive
    private int seatingCapacity;

    @Positive(message = "Fare per km must be greater than 0")
    private double farePerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    private Long driverId;

    public enum VehicleStatus {
        AVAILABLE, BOOKED, MAINTENANCE
    }

    public Vehicle() {}

    public Long getVehicleId() { return vehicleId; }
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
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
}
