package com.example.cafeteria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cafeterias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cafeteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private String name; // e.g., "Main Cafeteria (Big)", "East Wing (Small)"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    private String description; // Optional: "Has cloud kitchen, near elevator B"

    @Column(nullable = false)
    private Boolean isActive = true; // Admin can disable a cafeteria

    public enum Size {
        LARGE,   // 1 big cafeteria — has cloud kitchen
        MEDIUM,  // 1 medium cafeteria
        SMALL    // 2 small cafeterias
    }

}
