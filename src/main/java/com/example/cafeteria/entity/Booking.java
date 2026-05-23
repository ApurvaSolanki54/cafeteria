package com.example.cafeteria.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "bookings"
    // This is the KEY for preventing double bookings!
    // No two bookings can have the same table + same start_time.
    // If two people try simultaneously, database rejects the second one.
    // uniqueConstraints = {
    //     @UniqueConstraint(columnNames = {"table_id","start_time"})
    // }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Who made this booking? The person who initiated it.
     * They lose 1 coin just for booking.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    /*
     * Which table is being booked?
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private CafeteriaTable table;

    /* 2024-01-15 13:00:00 when lunch actually starts */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /* 2024-01-15 13:30:00 when lunch ends */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /* 2024-01-15 13:40:00 next booking allowed from here */
    @Column(nullable = false)
    private LocalDateTime windowEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BookingStatus {
        ACTIVE,    // Booking is valid and upcoming
        PENDING,    // someone started booking, not confirmed yet (expires in 1 min)
        COMPLETED, // Lunch time passed
        CANCELLED  // Someone cancelled it
    }
}
