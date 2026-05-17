
package com.example.cafeteria.service;

import com.example.cafeteria.entity.User;
import com.example.cafeteria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

/*
 * Spring Security needs to know: "Given an email, who is this user and what are their roles?"
 * This class answers that question.
 *
 * UserDetailsService is a Spring Security interface with ONE method: loadUserByUsername.
 * We implement it to load our User from PostgreSQL.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email. If not found, throw exception (Spring Security handles the rest)
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        System.err.println("user in loadUserByUsername " + user.getEmail() + " " + user.getPassword() + " " + user.getRole().name());
        /*
         * Spring Security needs a "ROLE_" prefix for roles.
         * So our "ADMIN" becomes "ROLE_ADMIN".
         * Our "EMPLOYEE" becomes "ROLE_EMPLOYEE".
         */
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}