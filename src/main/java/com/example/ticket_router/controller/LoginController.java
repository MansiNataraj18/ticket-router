package com.example.ticket_router.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the login page.
 * <p>
 * Authentication itself (handling the {@code POST /login} submission) is
 * performed entirely by Spring Security's form-login filter, configured in
 * {@link com.example.ticket_router.config.SecurityConfig}; this controller
 * only renders the view.
 */
@Controller
public class LoginController {

    /**
     * @return the {@code login} view template
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}