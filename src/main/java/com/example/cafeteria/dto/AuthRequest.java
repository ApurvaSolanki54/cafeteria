package com.example.cafeteria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * This is what the client SENDS to us for login.
 * We never expose our Entity (User.java) directly to the outside world.
 * Why? Because User has fields like password hash, coinBalance etc.
 * that we don't want to expose in every response.
 */
@Data // Lombok: generates getters, setters, toString, equals, hashCode
public class AuthRequest {

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
