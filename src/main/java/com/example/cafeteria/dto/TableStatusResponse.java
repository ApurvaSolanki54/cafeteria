// dto/TableStatusResponse.java
package com.example.cafeteria.dto;

import lombok.Data;
import java.time.LocalDateTime;

/*
 * This DTO is returned for EACH table in a cafeteria.
 * It tells the frontend everything it needs to draw the tile correctly:
 * - is it free or booked?
 * - if booked, WHEN exactly (actual booking time, not viewer's time)
 * - if booked, HOW MANY people are sitting (for the seat dots)
 */
@Data
public class TableStatusResponse {
    private Long id;
    private String tableNumber;
    private String tableType;
    private Integer minCapacity;
    private Integer maxCapacity;
    private boolean isActive;

    // FREE or BOOKED
    private String status;

    // Only filled when BOOKED:
    private LocalDateTime bookedFrom;  // actual start time e.g. 17:00
    private LocalDateTime bookedUntil; // actual end time e.g. 17:30
    private Integer occupiedSeats;     // booker + members count e.g. 3
}