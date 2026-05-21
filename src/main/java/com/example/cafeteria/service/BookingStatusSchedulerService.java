package com.example.cafeteria.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.cafeteria.entity.Booking;
import com.example.cafeteria.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingStatusSchedulerService {

    private final BookingRepository bookingRepository;

    /*
     * Runs every 10 seconds
     */
    @Scheduled(cron = "*/10 * * * * *")
    public void markExpiredBookings() {
        log.info("Starting expiry booking cron...");

        List<Booking> expiredBookings =
            bookingRepository.findByStatusAndWindowEndBefore(
                Booking.BookingStatus.ACTIVE,
                LocalDateTime.now()
            );
        System.out.println("expiredBookings "+ expiredBookings);
        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.COMPLETED);
        }

        bookingRepository.saveAll(expiredBookings);

        if (!expiredBookings.isEmpty()) {
            log.info("Updated bookings: " + expiredBookings.size());
            // System.out.println("Updated bookings: " + expiredBookings.size());
        }
    }
}