package com.example.cafeteria.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.cafeteria.dto.BookingRequest;
import com.example.cafeteria.dto.BookingResponse;
import com.example.cafeteria.entity.Booking;
import com.example.cafeteria.entity.BookingMember;
import com.example.cafeteria.entity.CafeteriaTable;
import com.example.cafeteria.entity.User;
import com.example.cafeteria.repository.BookingMemberRepository;
import com.example.cafeteria.repository.BookingRepository;
import com.example.cafeteria.repository.CafeteriaTableRepository;
import com.example.cafeteria.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMemberRepository memberRepository;
    private final CafeteriaTableRepository tableRepository;
    private final UserRepository userRepository;

    private static final int BOOKING_DURATION_MINUTES = 30;

    private static final int BUFFER_MINUTES = 10;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String bookerEmail) {
        // Step 1: Find the booker (person making the booking)
        User booker = userRepository.findByEmail(bookerEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Check if booker has enough coins
        if(booker.getCoinBalance() < 1) {
            throw new RuntimeException("Not enough coins to make a booking. Wait for next month!");
        }

        // Step 3: Find the table
        CafeteriaTable table = tableRepository.findById(request.getTableId())
        .orElseThrow(() -> new RuntimeException("Table not found"));

        // Step 4: Calculate the full time window
        LocalDateTime starTime = request.getStartTime();
        LocalDateTime endTime = starTime.plusMinutes(BOOKING_DURATION_MINUTES);
        LocalDateTime windowEnd = endTime.plusMinutes(BUFFER_MINUTES);

        /*
        * NEW — Check if this user already has a PENDING hold
        * for this exact table at this exact time.
        *
        * If YES -> just upgrade it to ACTIVE (no new row = no constraint violation)
        * If NO  -> check for conflicts and create fresh ACTIVE booking
        */
        Booking booking = bookingRepository
            .findPendingByTableAndTimeAndUser(
                table.getId(),
                starTime,
                booker.getId()
            )
            .orElse(null);

        if (booking != null) {
            /*
            * HAPPY PATH — user is confirming their own hold.
            * Just change status from PENDING -> ACTIVE.
            * No new row inserted -> no unique constraint violation.
            */
            booking.setStatus(Booking.BookingStatus.ACTIVE);
            booking = bookingRepository.save(booking);

        }
        else {
            /*
            * No existing hold — check for conflicts with OTHER people's bookings
            * then create a fresh ACTIVE booking.
            */
            //Step 5: Check for conflicts BEFORE saving.
            List<Booking> conflicts = bookingRepository.findConflictiBookings(table.getId(), starTime, windowEnd);
            if(!conflicts.isEmpty()) {
                throw new RuntimeException("Table is not available at this time. Please choose another slot.");
            }
    
            // Step 6: Create the booking
            booking = new Booking();
            booking.setBooker(booker);
            booking.setTable(table);
            booking.setStartTime(starTime);
            booking.setEndTime(endTime);
            booking.setWindowEnd(windowEnd);
            booking.setStatus(Booking.BookingStatus.ACTIVE);
    
            // Step 7: Save booking (if unique constraint violated, exception thrown here)
            try {
                booking = bookingRepository.save(booking);
            } catch (Exception e) {
                // Unique constraint violation = someone else just booked this table
                throw new RuntimeException("Table was just booked by someone else. Please try again!");
            }
        }
        // Step 8: Deduct 1 coin from booker
        booker.setCoinBalance(booker.getCoinBalance()-1);

        //Step 9: Add members if provided
        if(request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            addMembersToBooking(booking, request.getMemberIds());
        }
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse addMember(Long bookingId, Long memberId, String requesterEmail) {
        Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only the booker can add members
        if(!booking.getBooker().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("Only the person who booked can add members");
        }

        // Can't add to past or cancelled bookings
        if(booking.getStatus() != Booking.BookingStatus.ACTIVE) {
            throw new RuntimeException("Cannot add members to a completed or cancelled booking");
        }

        // Check max capacity
        long currentMemberCount = memberRepository.findByBookingId(bookingId).size();
        int maxCapacity = booking.getTable().getMaxCapacity();
        // +1 because the booker themselves also counts
        if(currentMemberCount + 1 >= maxCapacity) {
            throw new RuntimeException("Table is full! Max capacity: " + maxCapacity);
        }

        // Can't add yourself
        if(booking.getBooker().getId().equals(memberId)) {
            throw new RuntimeException("You're already the booker — don't add yourself again!");
        }

        // Check if already added
        if(memberRepository.existsByBookingIdAndUserId(bookingId, memberId)) {
            throw new RuntimeException("This person is already part of the booking");
        }

        User member = userRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("User not found"));

        // Check max capacity
        if(member.getCoinBalance() < 1) {
            throw new RuntimeException(member.getName() + " doesn't have enough coins!");
        }

        //Add member
        BookingMember bookingMember = new BookingMember();
        bookingMember.setBooking(booking);
        bookingMember.setUser(member);
        memberRepository.save(bookingMember);

        // Deduct coin from member
        member.setCoinBalance(member.getCoinBalance()-1);
        userRepository.save(member);
        return mapToResponse(booking);
    }

    // Helper method to add multiple members at once
    private void addMembersToBooking(Booking booking, List<Long> memberIds) {
        for(Long memberId: memberIds) {
            // Skip if it's the booker themselves
            if(booking.getBooker().getId().equals(memberId)) continue;
            // Skip if already added
            if(memberRepository.existsByBookingIdAndUserId(booking.getId(), memberId)) {
                continue;
            }
            User member = userRepository.findById(memberId)
            .orElse(null);
            if(member == null || member.getCoinBalance() < 1) continue;

            BookingMember bm = new BookingMember();
            bm.setBooking(booking);
            bm.setUser(member);
            memberRepository.save(bm);

            member.setCoinBalance(member.getCoinBalance()-1);
            userRepository.save(member);
        }
    }

    // Cancel a booking
    @Transactional
    public void cancelBooking(Long bookingId, String requesterEmail) {
        Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new RuntimeException("Booking not found"));

        if(!booking.getBooker().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("Only the booker can cancel this booking");
        }

        if(booking.getStatus() != Booking.BookingStatus.ACTIVE) {
            throw new RuntimeException("Booking is already cancelled or completed");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // NOTE: We do NOT refund coins on cancellation.
    }

    // Get all upcoming bookings for the logged-in user
    public List<BookingResponse>getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository
        .findByBookerIdAndStatusOrderByStartTimeAsc(user.getId(), Booking.BookingStatus.ACTIVE)
        .stream()
        .map(this::mapToResponse)
        .toList();
    }

    // Search colleagues by name (replaces Elasticsearch — KISS principle!)
    public List<?> searchColleagues(String name, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));

        return userRepository.searchByName(name, requester.getId())
        .stream()
        .map(u -> new java.util.HashMap<String, Object>() {{
            put("id", u.getId());
            put("name", u.getName());
            put("email", u.getEmail());
        }})
        .toList();
    }

    // Convert Booking entity -> BookingResponse DTO
    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookerName(booking.getBooker().getName());
        response.setCafeteriaName(booking.getTable().getCafeteria().getName());
        response.setTableNumber(booking.getTable().getTableNumber());
        response.setTableType(booking.getTable().getTableType().name());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setWindowEnd(booking.getWindowEnd());
        response.setStatus(booking.getStatus().name());
        response.setCreatedAt(booking.getCreatedAt());
        // Get list of member names
        List<String> memberNames = memberRepository.findByBookingId(booking.getId())
        .stream()
        .map(bm -> bm.getUser().getName())
        .toList();
        response.setMemberNames(memberNames);
        return response;
    }

    /*
    * Creates a PENDING booking — like "I am looking at this table".
    * Expires in 5 seconds if not confirmed.
    * Does NOT deduct coins (coins only deducted on ACTIVE booking).
    */
    @Transactional
    public Map<String, Object> holdTable(String tableId, String startTimeStr, String userEmail) {
        System.out.println("----------------hold table log--------------");
        User user  = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));

        CafeteriaTable table  = tableRepository.findById(Long.valueOf(tableId))
        .orElseThrow(() -> new RuntimeException("Table not found"));
        System.out.println("hold table " + table.getId());
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = startTime.plusMinutes(BOOKING_DURATION_MINUTES);
        LocalDateTime windowEnd = endTime.plusMinutes(BUFFER_MINUTES);

        // Cancel any existing PENDING hold by this user (clean up old holds)
        // so one user can't hold multiple tables simultaneously
        bookingRepository.findPendingByUser(user.getId()).forEach(b -> {
            b.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(b);
        });

        // Check no ACTIVE booking conflicts
        List<Booking> conflicts = bookingRepository.findConflictiBookings(
            table.getId(), startTime, windowEnd
        );
        if(!conflicts.isEmpty()) {
            throw new RuntimeException("Table already booked at this time.");
        }

        // Create PENDING booking — no coin deduction yet
        Booking hold = new Booking();
        hold.setBooker(user);
        hold.setTable(table);
        hold.setStartTime(startTime);
        hold.setEndTime(endTime);
        hold.setWindowEnd(windowEnd);
        hold.setStatus(Booking.BookingStatus.PENDING);
        hold = bookingRepository.save(hold);

        return Map.of(
            "holdId",   hold.getId(),
            "expiresIn", 5  // seconds
        );
    }
}
