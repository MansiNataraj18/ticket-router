package com.example.ticket_router.controller;


import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.exception.UserNotFoundException;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.service.TicketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * Serves the "My Tickets" page, showing a signed-in user's own ticket
 * submission history.
 */
@Controller
public class TicketPageController {

    private static final Logger log = LoggerFactory.getLogger(TicketPageController.class);

    private final TicketService ticketService;
    private final UserRepository userRepository;


    /**
     * @param ticketService  used to look up tickets belonging to the current user
     * @param userRepository used to look up the {@link User} associated with
     *                       the authenticated username
     */
    public TicketPageController(
            TicketService ticketService,
            UserRepository userRepository
    ) {

        this.ticketService = ticketService;
        this.userRepository = userRepository;

    }



    /**
     * Renders the authenticated user's ticket history.
     *
     * @param authentication the current request's authentication, or {@code null}/unauthenticated
     *                        if no user is logged in
     * @param model          the Spring MVC model to populate for the view
     * @return {@code "my-tickets"} if authenticated, otherwise a redirect to {@code /login}
     * @throws UserNotFoundException if the caller is authenticated but their
     *         account can no longer be found
     */
    @GetMapping("/my-tickets")
    public String myTickets(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {

            log.debug("Unauthenticated access to '/my-tickets' - redirecting to login");

            return "redirect:/login";

        }

        log.info("User '{}' viewed their ticket history", authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("VIEW_ALL_TICKETS"));

        boolean isDepartmentStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("VIEW_DEPARTMENT_TICKETS"));

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isDepartmentStaff", isDepartmentStaff);

        User user =
                userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UserNotFoundException(authentication.getName()));



        List<Ticket> tickets =
                ticketService
                        .getTicketsForUser(
                                user
                        );



        model.addAttribute(
                "tickets",
                tickets
        );


        return "my-tickets";

    }

}
