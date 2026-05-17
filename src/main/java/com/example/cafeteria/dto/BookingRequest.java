package com.example.cafeteria.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Table ID is required")
    private Long tableId;

    /*
     * @Future means: the booking time must be in the FUTURE.
     * You can't book a table for yesterday!
     */
    @NotNull(message = "Start time is required")
    @Future(message = "Booking time must be in the future")
    private LocalDateTime startTime;

    // Optional: list of colleague user IDs to add immediately
    // Can also add them later via a separate API call
    private List<Long> memberIds;
}