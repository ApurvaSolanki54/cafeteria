package com.example.cafeteria.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private String bookerName;
    private String cafeteriaName;
    private String tableNumber;
    private String tableType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime windowEnd;
    private String status;
    private List<String> memberNames;
    private LocalDateTime createdAt;
}
