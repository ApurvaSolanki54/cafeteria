package com.example.cafeteria.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.cafeteria.entity.CafeteriaTable;

@Repository
public interface CafeteriaTableRepository extends JpaRepository<CafeteriaTable, Long> {
    List<CafeteriaTable> findByCafeteriaIdAndIsActiveTrue(Long cafeteriaId);

    //Find tables that are AVAILABLE at the requested time.
    @Query("""
        SELECT t FROM CafeteriaTable t
        WHERE t.cafeteria.id = :cafeteriaId
        AND t.isActive = true
        and t.id NOT IN (
            SELECT b.table.id FROM Booking b
            WHERE b.status = 'ACTIVE'
            AND :requestedStartTime < b.windowEnd
            AND :requestedWindowEndTime > b.startTime
        )
    """)
    List<CafeteriaTable> findAvailableTables(
        Long cafeteriaId, 
        LocalDateTime requestedStartTime,
        LocalDateTime requestedWindowEndTime
    );
}
