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

    @NotNull(message = "Start time is required")
    @Future(message = "Booking time must be in the future")
    private LocalDateTime startTime;

    private List<Long> memberIds;
}