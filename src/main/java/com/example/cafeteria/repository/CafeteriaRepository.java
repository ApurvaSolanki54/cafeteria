package com.example.cafeteria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cafeteria.entity.Cafeteria;
import java.util.List;


@Repository
public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {
    // Get only active cafeterias (employees should only see active ones)
    List<Cafeteria> findByIsActiveTrue();
}
