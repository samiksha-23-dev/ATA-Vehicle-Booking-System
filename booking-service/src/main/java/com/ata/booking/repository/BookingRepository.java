package com.ata.booking.repository;

import com.ata.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    List<Booking> findByCustomerId(Long customerId);

    @Query("SELECT b FROM Booking b WHERE b.vehicleId = :vehicleId AND b.journeyDate = :journeyDate " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Booking> findActiveBookingsByVehicleAndDate(Long vehicleId, LocalDate journeyDate);

    @Query("SELECT b FROM Booking b WHERE b.driverId = :driverId AND b.journeyDate = :journeyDate " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Booking> findActiveBookingsByDriverAndDate(Long driverId, LocalDate journeyDate);

    @Query("SELECT COALESCE(SUM(b.passengerCount), 0) FROM Booking b WHERE b.vehicleId = :vehicleId " +
           "AND b.journeyDate = :journeyDate " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED')")
    int sumPassengersByVehicleAndDate(Long vehicleId, LocalDate journeyDate);

    List<Booking> findByStatusAndJourneyDate(Booking.BookingStatus status, LocalDate journeyDate);
}
