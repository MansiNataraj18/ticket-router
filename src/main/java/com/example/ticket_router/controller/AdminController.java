package com.example.ticket_router.controller;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.repository.TicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
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
     * @param authentication  the current request's authentication
     * @param priorityParam   optional priority filter ({@code HIGH}/{@code MEDIUM}/{@code LOW},
     *                        case-insensitive); when absent or invalid, all tickets are shown
     * @param sort            optional sort mode; {@code "priority"} orders by severity
     *                        (HIGH &rarr; LOW, ties broken by newest first), anything else
     *                        (or absent) orders by newest first
     * @param model           the Spring MVC model to populate for the view
     * @return {@code "admin"} if the caller has {@code ROLE_ADMIN},
     *         otherwise a redirect to {@code /}
     */
    @GetMapping("/admin")
    public String adminDashboard(
            Authentication authentication,
            @RequestParam(name = "priority", required = false) String priorityParam,
            @RequestParam(name = "sort", required = false) String sort,
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


        Priority priorityFilter = parsePriority(priorityParam);

        List<Ticket> tickets =
                priorityFilter != null
                        ? ticketRepository.findByPriority(priorityFilter)
                        : ticketRepository.findAll();

        boolean sortByPriority = "priority".equalsIgnoreCase(sort);

        Comparator<Ticket> comparator =
                sortByPriority
                        ? Comparator.comparing((Ticket t) -> t.getPriority().ordinal())
                                .reversed()
                                .thenComparing(Ticket::getCreatedAt, Comparator.reverseOrder())
                        : Comparator.comparing(Ticket::getCreatedAt, Comparator.reverseOrder());

        List<Ticket> sortedTickets = tickets.stream().sorted(comparator).toList();

        log.debug(
                "Admin dashboard: {} ticket(s) shown (priority filter={}, sort={})",
                sortedTickets.size(),
                priorityFilter,
                sortByPriority ? "priority" : "newest"
        );


        model.addAttribute("tickets", sortedTickets);
        model.addAttribute("selectedPriority", priorityFilter != null ? priorityFilter.name() : null);
        model.addAttribute("selectedSort", sortByPriority ? "priority" : null);



        return "admin";

    }

    /**
     * Parses a priority filter query parameter into a {@link Priority},
     * tolerating case differences and treating anything blank or unrecognized
     * as "no filter".
     *
     * @param priorityParam the raw {@code priority} query parameter value, or {@code null}
     * @return the matching {@link Priority}, or {@code null} if none was given or it was invalid
     */
    private Priority parsePriority(String priorityParam) {

        if (priorityParam == null || priorityParam.isBlank()) {
            return null;
        }

        try {
            return Priority.valueOf(priorityParam.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring invalid priority filter value: '{}'", priorityParam);
            return null;
        }
    }

}