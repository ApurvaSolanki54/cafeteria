package com.example.cafeteria.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cafeteria.dto.ApiResponse;
import com.example.cafeteria.dto.AuthRequest;
import com.example.cafeteria.dto.AuthResponse;
import com.example.cafeteria.dto.RegisterRequest;
import com.example.cafeteria.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

     /*
     * POST /api/auth/register
     * Anyone can register (no JWT needed — it's public)
     *
     * @Valid triggers validation on RegisterRequest (checks @NotBlank, @Email etc.)
     * @RequestBody means: read the request body as JSON and convert to RegisterRequest
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful!", response));
    }

    /*
     * POST /api/auth/login
     * Returns a JWT token to include in future requests.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody AuthRequest request
    ) {
        System.out.println("service " + request);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful!", response));
    }
    
}
