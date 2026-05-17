package com.example.cafeteria.controller;

/*
 * @RestControllerAdvice: This class "watches" all controllers.
 * When any controller throws an exception, this class catches it
 * and returns a proper JSON error response instead of ugly stack traces.
 */

import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.cafeteria.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle our custom business logic errors (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        System.out.println("error: "+ e);
        return ResponseEntity
        .badRequest()
        .body(ApiResponse.error(e.getMessage()));
    }
    
    // Handle validation errors (@Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>handleValidationErrors(MethodArgumentNotValidException e) { 
        String errors = e.getBindingResult().getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

        return ResponseEntity
        .badRequest()
        .body(ApiResponse.error("Validation failed: " + errors));
    }

    // Handle unauthorized access (wrong role)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied. You don't have permission to do this."));
    }
}
