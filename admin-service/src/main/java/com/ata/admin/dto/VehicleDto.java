package com.ata.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class VehicleDto {

    @NotBlank
    private String vehicleNumber;

    @NotBlank
    private String vehicleName;

    @NotBlank
    private String vehicleType;

    @Positive
    private int seatingCapacity;

    @Positive(message = "Fare per km must be greater than 0")
    private double farePerKm;

    private String status;

    private Long driverId;

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
