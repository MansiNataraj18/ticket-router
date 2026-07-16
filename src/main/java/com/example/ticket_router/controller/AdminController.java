package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.repository.TicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * Serves the admin dashboard, which lists every ticket submitted by every
 * user.
 * <p>
 * Access is additionally restricted at the security layer (see
 * {@link com.example.ticket_router.config.SecurityConfig}, which requires
 * {@code ROLE_ADMIN} for {@code /admin}); the in-method check here is a
 * defense-in-depth guard and provides the log message on unauthorized access.
 */
@Controller
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final TicketRepository ticketRepository;


    /**
     * @param ticketRepository repository used to load every submitted ticket
     */
    public AdminController(
            TicketRepository ticketRepository
    ) {

        this.ticketRepository = ticketRepository;

    }



    /**
     * Renders the admin dashboard listing all tickets, for admins only.
     *
     * @param authentication the current request's authentication
     * @param model          the Spring MVC model to populate for the view
     * @return {@code "admin"} if the caller has {@code ROLE_ADMIN},
     *         otherwise a redirect to {@code /}
     */
    @GetMapping("/admin")
    public String adminDashboard(
            Authentication authentication,
            Model model
    ) {

        boolean isAdmin =
                authentication != null &&
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority -> authority.equals("ROLE_ADMIN"));


        if (!isAdmin) {

            log.warn(
                    "Non-admin user '{}' attempted to access /admin",
                    authentication != null ? authentication.getName() : "anonymous"
            );

            return "redirect:/";

        }


        log.info("Admin '{}' opened the admin dashboard", authentication.getName());

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("isAdmin", true);


        List<Ticket> tickets =
                ticketRepository
                        .findAll();



        model.addAttribute(
                "tickets",
                tickets
        );



        return "admin";

    }

}