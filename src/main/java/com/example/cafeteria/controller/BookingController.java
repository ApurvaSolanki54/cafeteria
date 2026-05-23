package com.example.cafeteria.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cafeteria.dto.ApiResponse;
import com.example.cafeteria.dto.BookingRequest;
import com.example.cafeteria.dto.BookingResponse;
import com.example.cafeteria.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    /*
     * POST /api/bookings
     * Create a new table booking.
     *
     * @AuthenticationPrincipal UserDetails currentUser
     * This magic annotation gives us the currently logged-in user.
     * Spring Security fills this automatically from the JWT token.
     * We use currentUser.getUsername() to get their email.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
        @Valid @RequestBody BookingRequest request,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        System.out.println("currentUser " + currentUser);
        BookingResponse response = bookingService.createBooking(request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Table booked successfully!", response));
    }

    /*
     * POST /api/bookings/{bookingId}/members/{memberId}
     * Add a colleague to your booking.
     */
    @PostMapping("/{bookingId}/members/{memberId}")
    public ResponseEntity<ApiResponse<BookingResponse>> addMember(
        @PathVariable Long bookingId,// From URL: /api/bookings/5/members/12
        @PathVariable Long memberId,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        BookingResponse response = bookingService.addMember(bookingId, memberId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Member added!", response));
    }

    /*
     * DELETE /api/bookings/{bookingId}
     * Cancel a booking.
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
        @PathVariable Long bookingId,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        bookingService.cancelBooking(bookingId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled.", null));
    }

    /*
     * GET /api/bookings/my
     * Get all my upcoming bookings.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        List<BookingResponse> booking = bookingService.getMyBookings(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your bookings", booking));
    }

    /*
     * GET /api/bookings/search-colleagues?name=raj
     * Search for colleagues to add to your booking.
     * Replaces Elasticsearch with a simple DB query.
     */
    @GetMapping("/search-colleagues")
    public ResponseEntity<ApiResponse<?>> searchColleagues(
        @RequestParam String name,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        var results = bookingService.searchColleagues(name, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    /*
    * POST /api/bookings/hold
    * Creates a PENDING booking that expires in 1 minute.
    * Called when user SELECTS a table (not when they confirm).
    * Other users see this table as "pending" (yellow/orange).
    *
    * If user confirms -> status changes to ACTIVE
    * If user walks away -> a scheduled job cleans up PENDING after 1 min
    */
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<Map<String, Object>>> holdTable(
        @RequestBody Map<String, Object> body,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        String tableId = body.get("tableId").toString();
        String startTime = body.get("startTime").toString();
        Map<String, Object> result = bookingService.holdTable(
            tableId, startTime, currentUser.getUsername()
        );
        System.out.println("result hold "+ result);
        return ResponseEntity.ok(ApiResponse.success("Table held for 1 minute", result));
    }

}
