package com.example.ticket_router.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

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

                    .requestMatchers("/admin/users/**")
                    .hasAuthority("MANAGE_USERS")

                    .requestMatchers("/admin/**", "/admin")
                    .hasAuthority("VIEW_ALL_TICKETS")

                    .requestMatchers("/department/**")
                    .hasAuthority("VIEW_DEPARTMENT_TICKETS")

                    .anyRequest()
                    .authenticated()
            )

            .formLogin(login -> login
                    .loginPage("/login")
                    .successHandler(roleBasedSuccessHandler())
                    .failureHandler(loggingFailureHandler())
                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutSuccessHandler(loggingLogoutSuccessHandler())
            );


        return http.build();
    }


    @Bean
    AuthenticationSuccessHandler roleBasedSuccessHandler() {

        return (request, response, authentication) -> {

            boolean canViewAllTickets = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("VIEW_ALL_TICKETS"));

            boolean isDepartmentStaff = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("VIEW_DEPARTMENT_TICKETS"));

            log.info(
                    "Login succeeded for user '{}' (authorities: {})",
                    authentication.getName(),
                    authentication.getAuthorities()
            );

            String destination =
                    canViewAllTickets ? "/admin"
                    : isDepartmentStaff ? "/department"
                    : "/";

            response.sendRedirect(destination);
        };
    }


    @Bean
    AuthenticationFailureHandler loggingFailureHandler() {

        return (request, response, exception) -> {

            log.warn(
                    "Login failed for user '{}': {}",
                    request.getParameter("username"),
                    exception.getMessage()
            );

            response.sendRedirect(request.getContextPath() + "/login?error");
        };
    }


    @Bean
    LogoutSuccessHandler loggingLogoutSuccessHandler() {

        return (request, response, authentication) -> {

            if (authentication != null) {
                log.info("User '{}' logged out", authentication.getName());
            }

            response.sendRedirect(request.getContextPath() + "/login?logout");
        };
    }
}