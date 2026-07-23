package com.example.ticket_router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides shared security-related beans used across the application, kept
 * separate from {@link SecurityConfig} to avoid a circular dependency
 * between the password encoder and the security filter chain setup.
 */
@Configuration
public class SecurityBeans {

    /**
     * @return the {@link PasswordEncoder} used to hash and verify user
     *         passwords ({@link BCryptPasswordEncoder})
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
