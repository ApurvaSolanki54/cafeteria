package com.example.cafeteria.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TableStatusResponse {
    private Long id;
    private String tableNumber;
    private String tableType;
    private Integer minCapacity;
    private Integer maxCapacity;
    private boolean isActive;    
    private String status;

    // Only filled when BOOKED:
    private LocalDateTime bookedFrom;  // actual start time e.g. 17:00
    private LocalDateTime bookedUntil; // actual end time e.g. 17:30
    private Integer occupiedSeats;     // booker + members count e.g. 3

    private String bookingStatus; // "ACTIVE" or "PENDING"
}