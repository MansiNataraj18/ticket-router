package com.example.ticket_router.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the ticket submission home page.
 * <p>
 * Unauthenticated visitors are redirected to {@code /login}; authenticated
 * users (any role) see the "Submit a Ticket" page, which calls
 * {@link com.example.ticket_router.controller.TicketController} via AJAX to
 * classify tickets.
 */
@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    /**
     * Renders the ticket submission page for authenticated users, exposing
     * the current username and admin status (used by the shared navbar
     * fragment) as model attributes.
     *
     * @param authentication the current request's authentication, or {@code null}/unauthenticated
     *                        if no user is logged in
     * @param model          the Spring MVC model to populate for the view
     * @return {@code "index"} if authenticated, otherwise a redirect to {@code /login}
     */
    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Unauthenticated access to '/' - redirecting to login");
            return "redirect:/login";
        }

        log.info("User '{}' opened the ticket submission page", authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("VIEW_ALL_TICKETS"));

        boolean isDepartmentStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("VIEW_DEPARTMENT_TICKETS"));

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isDepartmentStaff", isDepartmentStaff);

        return "index";
    }
}
