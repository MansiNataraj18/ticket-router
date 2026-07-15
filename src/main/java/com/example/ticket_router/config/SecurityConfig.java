package com.example.ticket_router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;


    public SecurityConfig(
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
    }


    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/login",
                            "/css/**"
                    )
                    .permitAll()

                    .anyRequest()
                    .authenticated()
            )

            .formLogin(login -> login
                    .loginPage("/login")
                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutSuccessUrl("/login")
            );


        return http.build();
    }
}