package com.example.cafeteria.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.cafeteria.entity.Booking;
import com.example.cafeteria.repository.BookingRepository;
import com.example.cafeteria.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * @Slf4j: Lombok generates a logger so we can do log.info("...") easily
 * This service runs on a schedule — no HTTP request needed!
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoinRefillService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.coins.monthly-refill}")
    private Integer monthlyCoins;

    @Scheduled(cron = "0 0 1 1 * *") // 1st of every month at midnight
    @Transactional
    public void refillCoinsForAllEmployees() {
        log.info("Starting monthly coin refill for all employees...");
        // Load ALL users from DB
        //var -> “Let Java automatically figure out the variable type.”
        var allUsers = userRepository.findAll();
        // Reset every user's coin balance to the monthly amount
        allUsers.forEach(user -> user.setCoinBalance(monthlyCoins));

        // Save all at once (more efficient than saving one by one)
        userRepository.saveAll(allUsers);

        log.info("Monthly coin refill complete! {} employees refilled with {} coins each.",
            allUsers.size(), monthlyCoins);
    }

    /*
    * Runs every 10 seconds.
    * Finds all PENDING bookings older than 5 seconds and cancels them.
    * This releases the "hold" if user walked away without confirming.
    *
    * cron = "0/10 * * * * *" means: every 10 seconds
    */
    @Scheduled(cron = "0/10 * * * * *")
    @Transactional
    public void cancelExpiredHolds() {
        LocalDateTime fiveSecondsAgo = LocalDateTime.now().minusSeconds(5);

        // Find PENDING bookings created more than 5 seconds ago
        bookingRepository.findAll().stream()
        .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING
            && b.getCreatedAt().isBefore(fiveSecondsAgo))
        .forEach(b -> {
            b.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(b);
        });
    }
}
