package com.ata.booking.scheduler;

import com.ata.booking.entity.Booking;
import com.ata.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BookingStatusScheduler.class);

    private final BookingRepository bookingRepository;

    public BookingStatusScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void updateBookingStatuses() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Booking> toOngoing = bookingRepository.findByStatusAndJourneyDate(
                Booking.BookingStatus.CONFIRMED, today);
        toOngoing.forEach(b -> b.setStatus(Booking.BookingStatus.ONGOING));
        bookingRepository.saveAll(toOngoing);
        logger.info("Transitioned {} bookings to ONGOING", toOngoing.size());

        List<Booking> toCompleted = bookingRepository.findByStatusAndJourneyDate(
                Booking.BookingStatus.ONGOING, yesterday);
        toCompleted.forEach(b -> b.setStatus(Booking.BookingStatus.COMPLETED));
        bookingRepository.saveAll(toCompleted);
        logger.info("Transitioned {} bookings to COMPLETED", toCompleted.size());
    }
}
