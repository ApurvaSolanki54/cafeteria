package com.example.cafeteria.config;


import com.example.cafeteria.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * @Configuration: This class has settings Spring should load at startup.
 * @EnableWebSecurity: Turn on Spring Security for our web app.
 * @EnableMethodSecurity: Allows us to use @PreAuthorize on methods.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /*
     * THE MAIN SECURITY RULES.
     * Think of this as the "rulebook" for who can access what.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for REST APIs — CSRF is for browser form submissions)
            .csrf(AbstractHttpConfigurer::disable)

            // Define who can access which URLs
            .authorizeHttpRequests(auth -> auth
                // These endpoints are PUBLIC — no login required
                .requestMatchers("/api/auth/**").permitAll()

                // Only ADMIN can access these
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else requires login (any role)
                .anyRequest().authenticated()
            )

            // Use STATELESS sessions — no server-side session storage.
            // Every request must carry a JWT. Server doesn't remember you.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Add our JWT filter BEFORE Spring's built-in login filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * BCrypt is the industry standard for hashing passwords.
     * It's slow BY DESIGN — making brute-force attacks impractical.
     * Password "hello123" → "$2a$10$..." (different hash each time!)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * AuthenticationProvider: Knows HOW to verify a username/password.
     * DaoAuthenticationProvider: loads user from DB, compares hashed passwords.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /*
     * AuthenticationManager: Orchestrates the authentication process.
     * We inject this in AuthService to call manager.authenticate(...)
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
