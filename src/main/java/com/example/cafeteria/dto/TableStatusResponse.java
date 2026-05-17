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
    private LocalDateTime bookedFrom;  
    private LocalDateTime bookedUntil; 
    private Integer occupiedSeats;     
}