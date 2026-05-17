package com.example.cafeteria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cafeteria.entity.BookingMember;

@Repository
public interface BookingMemberRepository extends JpaRepository<BookingMember, Long> {
    List<BookingMember> findByBookingId(Long bookingId);

    // Check if user already added to this booking
    boolean existsByBookingIdAndUserId(Long bookingId, Long userId);

}
