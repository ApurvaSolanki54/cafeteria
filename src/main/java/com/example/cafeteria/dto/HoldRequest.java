package com.example.cafeteria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * Request body for POST /api/bookings/hold
 * Using a proper DTO instead of Map<String, Object>
 * avoids type conversion issues with JSON numbers.
 */
@Data
public class HoldRequest {

    @NotNull(message = "Table ID is required")
    private Long tableId;       // Jackson maps JSON number directly to Long ✅

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;  // Jackson parses ISO datetime string ✅
}
