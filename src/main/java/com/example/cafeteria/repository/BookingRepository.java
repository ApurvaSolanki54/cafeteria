package com.example.cafeteria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.cafeteria.entity.Booking;
import com.example.cafeteria.entity.Booking.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long>{
    List<Booking> findByBookerIdAndStatusOrderByStartTimeAsc(Long bookerId, Booking.BookingStatus status);

    /*
     * Check if a table has ANY conflicting booking.
     * Used as double-check before saving a new booking.
     */
    
    @Query("""
        SELECT b FROM Booking b
        WHERE b.table.id = :tableId
        AND b.status = 'ACTIVE'
        AND :newStartTime < b.windowEnd
        AND :newWindowEndTime > b.startTime
    """)
    List<Booking> findConflictiBookings(
        Long tableId, 
        LocalDateTime newStartTime,
        LocalDateTime newWindowEndTime
    );

    // Find all active bookings for a table on a given day
    @Query("""
        SELECT b FROM Booking b
        WHERE b.table.id = :tableId
        AND b.status = 'ACTIVE'
        AND b.startTime >= :dayStart
        AND b.startTime < :dayEnd
        ORDER BY b.startTime ASC
        """)
    List<Booking> findBookingsForTableOnDay(
        Long tableId, 
        LocalDateTime dayStart, 
        LocalDateTime dayEnd
    );

        /*
    * Find the ACTIVE booking for a specific table at a specific time window.
    * Used by frontend to show "Booked at 1:00 PM with 3 people" on the red tile.
    *
    * We check: is there a booking whose window covers the requested time?
    * meaning: requestedTime falls inside (startTime -> windowEnd)
    */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.table.id = :tableId
        AND b.status IN ('ACTIVE', 'PENDING')
        AND :requestedTime >= b.startTime
        AND :requestedTime < b.windowEnd
        """)
    Optional<Booking> findActiveBookingAtTime(
        Long tableId,
        LocalDateTime requestedTime
    );

    List<Booking> findByStatusAndWindowEndBefore(
        BookingStatus status,
        LocalDateTime time
    );

    // Find all PENDING holds by a user (to cancel old ones)
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.status = 'PENDING'")
    List<Booking> findPendingByUser(Long userId);

    /*
    * Find a PENDING booking made by THIS specific user
    * for THIS specific table at THIS specific start time.
    *
    * Used in createBooking to check:
    * "Did this user already hold this table?"
    * If yes -> upgrade to ACTIVE instead of inserting new row.
    */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.table.id   = :tableId
        AND   b.startTime  = :startTime
        AND   b.booker.id  = :userId
        AND   b.status     = 'PENDING'
        """)
    Optional<Booking> findPendingByTableAndTimeAndUser(
        Long tableId,
        LocalDateTime startTime,
        Long userId
    );
}
