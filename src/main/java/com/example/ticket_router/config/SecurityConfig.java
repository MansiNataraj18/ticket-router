package com.example.ticket_router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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
                            "/signup",
                            "/css/**"
                    )
                    .permitAll()

                    .requestMatchers("/admin")
                    .hasRole("ADMIN")

                    .anyRequest()
                    .authenticated()
            )

            .formLogin(login -> login
                    .loginPage("/login")
                    .successHandler(roleBasedSuccessHandler())
                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
            );


        return http.build();
    }


    @Bean
    AuthenticationSuccessHandler roleBasedSuccessHandler() {

        return (request, response, authentication) -> {

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            response.sendRedirect(isAdmin ? "/admin" : "/");
        };
    }
}