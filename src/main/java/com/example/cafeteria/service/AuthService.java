package com.example.cafeteria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cafeteria.config.JwtConfig;
import com.example.cafeteria.dto.AuthRequest;
import com.example.cafeteria.dto.AuthResponse;
import com.example.cafeteria.dto.RegisterRequest;
import com.example.cafeteria.entity.User;
import com.example.cafeteria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    @Value("${app.coins.monthly-refill}")
    private Integer monthlyCoins;

    public AuthResponse register(RegisterRequest request) {
        // Check if email already taken
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registerd");
        }
        //create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.EMPLOYEE);
        user.setCoinBalance(monthlyCoins);
        
        user = userRepository.save(user);
        // Generate JWT token so they're immediately logged in
        String token = jwtConfig.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name(), user.getCoinBalance());
    }

    public AuthResponse login(AuthRequest request) {
        // This line does the actual verification
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        System.out.println("reached");
        // If we reach here, authentication succeeded
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtConfig.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name(), user.getCoinBalance());
    }
}
