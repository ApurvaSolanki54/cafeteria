package com.example.cafeteria.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.cafeteria.dto.TableStatusResponse;
import com.example.cafeteria.entity.Cafeteria;
import com.example.cafeteria.entity.CafeteriaTable;
import com.example.cafeteria.repository.BookingMemberRepository;
import com.example.cafeteria.repository.BookingRepository;
import com.example.cafeteria.repository.CafeteriaRepository;
import com.example.cafeteria.repository.CafeteriaTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CafeteriaService {
    private final CafeteriaRepository cafeteriaRepository;
    private final CafeteriaTableRepository tableRepository;
    private final BookingRepository bookingRepository;
    private final BookingMemberRepository bookingMemberRepository;

    private static final int BOOKING_DURATION_MINUTES = 30;
    private static final int BUFFER_MINUTES = 10;

    public Cafeteria addCafeteria(@NonNull Cafeteria cafeteria) {
        return cafeteriaRepository.save(cafeteria);
    }

    public List<Cafeteria> getAllCafeterias() {
        return cafeteriaRepository.findAll();
    }

    public List<Cafeteria>getActivCafeterias() {
        return cafeteriaRepository.findByIsActiveTrue();
    }

    public Cafeteria updateCafeteria(@NonNull Long id, Cafeteria updated) {
        Cafeteria existing = cafeteriaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Cafeteria not found"));

        existing.setName(updated.getName());
        existing.setSize(updated.getSize());
        existing.setDescription(updated.getDescription());
        existing.setIsActive(updated.getIsActive());

        return cafeteriaRepository.save(existing);
    }

    public CafeteriaTable addTable(@NonNull CafeteriaTable table) {
        return tableRepository.save(table);
    }

    public List<CafeteriaTable> getTablesByCafeteria(Long cafeteriaId) {
        return tableRepository.findByCafeteriaIdAndIsActiveTrue(cafeteriaId);
    }

    public List<CafeteriaTable> getAvailableTables(Long cafeteriaId, LocalDateTime startTime) {
        // Full window = startTime to (startTime + 30min + 10min)
        LocalDateTime windowEnd = startTime
        .plusMinutes(BOOKING_DURATION_MINUTES)
        .plusMinutes(BUFFER_MINUTES);

        return tableRepository.findAvailableTables(cafeteriaId, startTime, windowEnd);
    }

    /*
    * For each table in the cafeteria:
    * 1. Check if there's an active booking at the requested time
    * 2. If YES → status = BOOKED, fill bookedFrom, bookedUntil, occupiedSeats
    * 3. If NO  → status = FREE
    *
    * occupiedSeats = 1 (booker) + number of members added
    * This drives the dark/light red seat dots in frontend.
    */
    public List<TableStatusResponse> getTableStatuses(Long cafeteriaId, LocalDateTime requestedTime) {
        List<CafeteriaTable> tables = tableRepository.findByCafeteriaIdAndIsActiveTrue(cafeteriaId);

        return tables.stream().map(table -> {
            TableStatusResponse resp = new TableStatusResponse();
            resp.setId(table.getId());
            resp.setTableNumber(table.getTableNumber());
            resp.setTableType(table.getTableType().name());
            resp.setMinCapacity(table.getMinCapacity());
            resp.setMaxCapacity(table.getMaxCapacity());
            resp.setActive(table.getIsActive());

            // Check if this table has an active booking at requestedTime
            bookingRepository.findActiveBookingAtTime(table.getId(), requestedTime)
            .ifPresentOrElse(
                booking -> {
                    // BOOKED — fill all the booking details
                    resp.setStatus("BOOKED");
                    resp.setBookingStatus(booking.getStatus().name()); // "ACTIVE" or "PENDING"
                    resp.setBookedFrom(booking.getStartTime());   // actual time e.g. 17:00
                    resp.setBookedUntil(booking.getEndTime());

                    // Count: 1 booker + number of members
                    int memberCount = bookingMemberRepository
                        .findByBookingId(booking.getId()).size();
                    resp.setOccupiedSeats(1 + memberCount); // 1 = the booker themselves
                },
                () -> {
                    // FREE — no booking at this time
                    resp.setStatus("FREE");
                    resp.setOccupiedSeats(0);
                }
            );
            return resp;
        }).toList();
    }
}
