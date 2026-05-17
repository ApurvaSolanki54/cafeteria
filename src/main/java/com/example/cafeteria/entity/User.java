package com.example.cafeteria.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "users")
@Getter         // Lombok generates all getters automatically
@Setter         // Lombok generates all setters automatically
@NoArgsConstructor   // Lombok generates empty constructor: new User()
@AllArgsConstructor  // Lombok generates full constructor with all fields
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Integer coinBalance = 30;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, EMPLOYEE
    }
}
