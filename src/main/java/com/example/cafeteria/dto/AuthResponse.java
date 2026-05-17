package com.example.cafeteria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * What we SEND BACK after successful login.
 * The JWT token is what the client will include in all future requests.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String role;
    private Integer coinBalance;
}
