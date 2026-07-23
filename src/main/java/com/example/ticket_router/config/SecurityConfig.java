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

/**
 * Configures Spring Security's HTTP filter chain and RBAC-based URL
 * authorization: form login, logout, and per-path authority requirements
 * (e.g. {@code MANAGE_USERS} for admin user management, {@code
 * VIEW_ALL_TICKETS} for the admin console, {@code VIEW_DEPARTMENT_TICKETS}
 * for department views). Also defines the login success/failure and logout
 * handlers used to redirect users based on role and to log auth events.
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Defines the RBAC authorization rules and form login/logout handlers
     * for the application: public login/signup/static assets, authority
     * restrictions on {@code /admin/**} and {@code /department/**}, and
     * authentication required for everything else.
     *
     * @param http the security configuration builder
     * @return the assembled {@link SecurityFilterChain}
     * @throws Exception if the security configuration fails to build
     */
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

    /**
     * @return a handler that redirects a user after successful login based
     *         on their authorities: {@code /admin} for {@code
     *         VIEW_ALL_TICKETS}, {@code /department} for {@code
     *         VIEW_DEPARTMENT_TICKETS}, otherwise {@code /}
     */
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

    /**
     * @return a handler that logs a warning on failed login attempts and
     *         redirects back to {@code /login?error}
     */
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

    /**
     * @return a handler that logs successful logouts and redirects to
     *         {@code /login?logout}
     */
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
