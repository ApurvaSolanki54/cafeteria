package com.example.cafeteria.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/*
 * This is the "bouncer" of our application.
 *
 * OncePerRequestFilter: runs ONCE for every HTTP request that comes in.
 *
 * What it does:
 * 1. Look for "Authorization: Bearer <token>" header in the request
 * 2. Extract the JWT token
 * 3. Validate it
 * 4. If valid, tell Spring Security "this user is authenticated"
 * 5. Let the request continue to the controller
 *
 * If no token or bad token → request proceeds but as "anonymous user"
 * Spring Security then blocks it if the endpoint requires authentication.
 */
@Component
@RequiredArgsConstructor // Lombok: generates constructor for all final fields
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Look for the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header, or it doesn't start with "Bearer ", skip JWT check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("inside if");
            filterChain.doFilter(request, response); // Continue to next filter
            return;
        }

        // Step 2: Extract token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        System.out.println("jwt "+ jwt);
        // Step 3: Get email from token
        final String email = jwtConfig.extractEmail(jwt);
        System.out.println("email "+email);

        // Step 4: If we got an email and user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            System.out.println("if condition userDetails "+ userDetails);
            // Validate the token
            if (jwtConfig.isTokenValid(jwt, userDetails)) {
                // Create an "authentication ticket" for Spring Security
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                         // No password needed (already verified)
                        userDetails.getAuthorities()  // Roles: ADMIN or EMPLOYEE
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Tell Spring Security: "This user is logged in for this request"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 5: Continue processing the request
        filterChain.doFilter(request, response);
    }
}