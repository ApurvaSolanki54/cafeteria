package com.example.cafeteria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        /*
        * Allow requests from:
        * 1. Local React dev server
        * 2. Your deployed Vercel frontend
        *
        * Replace the Vercel URL with your actual Vercel URL
        * after you deploy the frontend.
        */
        // Allow frontend URLs
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://cafeteria-frontend-wheat.vercel.app");

        // Allow all HTTP methods
        config.addAllowedMethod("*");
        // Allow all headers
        config.addAllowedHeader("*");
        // Allow cookies/auth headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
