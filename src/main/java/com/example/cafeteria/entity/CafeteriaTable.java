package com.example.cafeteria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cafeteria_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cafeteria_id", nullable = false)
    private Cafeteria cafeteria;

    @Column(nullable = false)
    private String tableNumber;// e.g., "A1", "B3"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableType tableType;

    @Column(nullable = false)
    private Integer minCapacity; // BIG=1, SMALL=1

    @Column(nullable = false)
    private Integer maxCapacity; // BIG=7, SMALL=4

    @Column(nullable = false)
    private Boolean isActive = true;

    public enum TableType {
        BIG,   // 6-7 seats, low chance of 1-2 people
        SMALL  // 4 seats max, common for 1-2 people
    }
}
