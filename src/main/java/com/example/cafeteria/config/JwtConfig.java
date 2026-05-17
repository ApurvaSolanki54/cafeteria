package com.example.cafeteria.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

//* @Component marks this as a Spring-managed bean.
//Spring will create ONE instance of this class and reuse it everywhere.
@Component
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // 86400000 ms = 24 hours

    // Convert our secret string into a cryptographic key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /*
     * CREATE a JWT token for a logged-in user.
     * Called after successful login.
     */
    public String generateToken(String email) {
        return Jwts.builder()
        .subject(email) // Who is this token for?
        .issuedAt(new Date()) // When was it created?
        .expiration(new Date(System.currentTimeMillis() + expiration)) // When does it expire?
        .signWith(getSigningKey()) // Sign it with our secret
        .compact(); // Build the final string
    }

    /*
     * READ the email from a JWT token.
     * Called on every request to identify who is making the request.
     */
    public String extractEmail(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey()) // Verify the signature is valid
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject(); // Get the email we stored
    }


    /*
     * CHECK if a token is valid for a user.
     * Verifies both: correct user AND not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            System.err.println("ERROR IN isTokenValid FUNC " + e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiry = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getExpiration();

        return expiry.before(new Date());
    }
}
