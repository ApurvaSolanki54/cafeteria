package com.example.cafeteria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.cafeteria.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);

    // For searching colleagues by name
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) AND u.id != :excludeUserId")
    List<User> searchByName(String name, Long excludeUserId);

    // Check if email already exists (for registration)
    boolean existsByEmail(String email);
}
