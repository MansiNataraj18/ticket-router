package com.example.ticket_router.controller;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketStatus;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.TicketNotFoundException;
import com.example.ticket_router.exception.UserNotFoundException;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.service.TicketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serves each department's own ticket dashboard.
 * <p>
 * There is a single controller/template shared by all five departments —
 * "separate views per department" is achieved by every staff member only
 * ever seeing tickets whose {@code assignedTeam} matches their own {@link
 * UserType#getDepartmentTeamName()}, both when listing tickets and when
 * acting on one (see {@link #requireOwnDepartmentTicket}).
 */
@Controller
@RequestMapping("/department")
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    /**
     * @param ticketService    applies status changes and the rejected-only delete rule
     * @param ticketRepository used to look up a single ticket for ownership checks
     * @param userRepository   used to resolve the authenticated user's department
     */
    public DepartmentController(
            TicketService ticketService,
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Renders the current user's department dashboard.
     *
     * @param authentication the current request's authentication
     * @param priorityParam  optional priority filter ({@code HIGH}/{@code MEDIUM}/{@code LOW},
     *                       case-insensitive); when absent or invalid, all priorities are shown
     * @param model          the Spring MVC model to populate for the view
     * @return {@code "department"} for department staff, otherwise a redirect to {@code /}
     */
    @GetMapping
    public String dashboard(
            Authentication authentication,
            @RequestParam(name = "priority", required = false) String priorityParam,
            Model model
    ) {
        User user = currentUser(authentication);
        UserType userType = user.getUserType();

        if (!userType.isDepartmentStaff()) {
            log.warn(
                    "User '{}' with non-department user type '{}' attempted to access /department",
                    user.getUsername(),
                    userType.getName()
            );
            return "redirect:/";
        }

        String department = userType.getDepartmentTeamName();
        Priority priorityFilter = parsePriority(priorityParam);
        log.info(
                "User '{}' opened the {} dashboard (priority filter={})",
                user.getUsername(),
                department,
                priorityFilter
        );

        List<Ticket> tickets = ticketService.getTicketsForDepartment(department, priorityFilter);

        model.addAttribute("userName", user.getUsername());
        model.addAttribute("isAdmin", false);
        model.addAttribute("isDepartmentStaff", true);
        model.addAttribute("departmentName", department);
        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedPriority", priorityFilter != null ? priorityFilter.name() : null);

        return "department";
    }

    /**
     * Updates a ticket's workflow status, restricted to tickets belonging to
     * the caller's own department.
     *
     * @param id             the ticket to update
     * @param status         the new status name ({@code PENDING}/{@code ACCEPTED}/
     *                       {@code REJECTED}/{@code IN_PROGRESS}/{@code COMPLETED})
     * @param authentication the current request's authentication
     * @return a redirect back to {@code /department}
     */
    @PostMapping("/tickets/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        requireOwnDepartmentTicket(id, user);

        TicketStatus newStatus;
        try {
            newStatus = TicketStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring invalid status value '{}' for ticket {}", status, id);
            return "redirect:/department";
        }

        ticketService.updateStatus(id, newStatus);
        log.info("User '{}' set ticket {} status to {}", user.getUsername(), id, newStatus);
        return "redirect:/department";
    }

    /**
     * Deletes a ticket, restricted to tickets belonging to the caller's own
     * department that have already been marked {@code REJECTED}.
     *
     * @param id             the ticket to delete
     * @param authentication the current request's authentication
     * @return a redirect back to {@code /department}
     * @throws InvalidTicketException if the ticket is not currently REJECTED
     */
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        requireOwnDepartmentTicket(id, user);

        ticketService.deleteRejectedTicket(id);
        log.info("User '{}' deleted rejected ticket {}", user.getUsername(), id);
        return "redirect:/department";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));
    }

    /**
     * Loads a ticket and verifies it belongs to the given user's department,
     * so one department's staff can never view, act on, or guess IDs into
     * another department's tickets.
     */
    private Ticket requireOwnDepartmentTicket(Long ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        String department = user.getUserType().getDepartmentTeamName();
        if (department == null || !department.equals(ticket.getAssignedTeam())) {
            log.warn(
                    "User '{}' attempted to act on ticket {} outside their department",
                    user.getUsername(),
                    ticketId
            );
            throw new InvalidTicketException("That ticket does not belong to your department");
        }

        return ticket;
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
