package com.example.cafeteria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * A standard wrapper for ALL our API responses.
 * Instead of returning raw objects, we wrap everything like:
 * { "success": true, "message": "Booking created!", "data": {...} }
 *
 * This makes your API consistent and professional.
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Quick factory methods for convenience
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}