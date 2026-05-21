package com.example.cafeteria.service;


import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.cafeteria.entity.User;
import com.example.cafeteria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Map<String, Object> getMe(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return Map.of(
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "coinBalance", user.getCoinBalance()
        );
    }
}