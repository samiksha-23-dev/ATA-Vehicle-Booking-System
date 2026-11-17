package com.ata.admin.repository;

import com.ata.admin.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByVehicleNumber(String vehicleNumber);
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);
}
