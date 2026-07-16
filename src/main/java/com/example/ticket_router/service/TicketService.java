package com.example.ticket_router.service;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.dto.TicketStatus;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.TicketNotFoundException;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Persists submitted tickets, retrieves a given user's or department's
 * tickets, and applies the department/admin ticket workflow (status changes
 * and the conditional delete rules for each side).
 */
@Service
public class TicketService {


    private final TicketRepository ticketRepository;


    /**
     * @param ticketRepository repository used to persist and query tickets
     */
    public TicketService(
            TicketRepository ticketRepository
    ) {

        this.ticketRepository = ticketRepository;

    }


    /**
     * Saves a newly routed ticket against the given user.
     *
     * @param message the original ticket message
     * @param user    the user who submitted the ticket
     * @param result  the AI-generated category, priority, team, and reasoning
     * @return the persisted {@link Ticket}, including its generated ID
     * @throws InvalidTicketException if {@code message} is blank, {@code user}
     *         is null, or {@code result} is null/incomplete
     */
    public Ticket saveTicket(
        String message,
        User user,
        TicketRoutingResult result
) {


    validateTicketData(
            message,
            user,
            result
    );


    Ticket ticket = new Ticket();


        ticket.setMessage(message);

        ticket.setCategory(
                result.category()
        );


        ticket.setPriority(
                result.priority()
        );


        ticket.setAssignedTeam(
                result.assignedTeam()
        );


        ticket.setReasoning(
                result.reasoning()
        );


        ticket.setUser(
                user
        );


        return ticketRepository.save(ticket);

    }

    /**
     * Retrieves a user's submitted tickets, most recent first.
     *
     * @param user the user to look up tickets for
     * @return the user's tickets ordered by creation date, descending
     */
    public List<Ticket> getTicketsForUser(
        User user
) {

    return ticketRepository
            .findByUserOrderByCreatedAtDesc(user);

}

    /**
     * Retrieves every ticket assigned to a department, most recent first,
     * optionally narrowed down to a single priority level.
     *
     * @param assignedTeam   the exact team name (e.g. "Engineering Department")
     * @param priorityFilter the priority to restrict results to, or {@code null} for all priorities
     * @return that department's tickets (optionally filtered by priority), most recently created first
     */
    public List<Ticket> getTicketsForDepartment(String assignedTeam, Priority priorityFilter) {

        return priorityFilter != null
                ? ticketRepository.findByAssignedTeamAndPriorityOrderByCreatedAtDesc(assignedTeam, priorityFilter)
                : ticketRepository.findByAssignedTeamOrderByCreatedAtDesc(assignedTeam);

    }

    /**
     * Updates a ticket's workflow status (accepted/rejected/in progress/completed).
     *
     * @param ticketId  the ticket to update
     * @param newStatus the status to set
     * @return the updated ticket
     * @throws TicketNotFoundException if no ticket exists with the given id
     */
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setStatus(newStatus);

        return ticketRepository.save(ticket);

    }

    /**
     * Deletes a ticket, but only if the department has already marked it
     * {@link TicketStatus#REJECTED} — this is the one deletion a department
     * is allowed to perform.
     *
     * @param ticketId the ticket to delete
     * @throws TicketNotFoundException if no ticket exists with the given id
     * @throws InvalidTicketException  if the ticket is not currently REJECTED
     */
    public void deleteRejectedTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (ticket.getStatus() != TicketStatus.REJECTED) {

            throw new InvalidTicketException(
                    "Only tickets marked REJECTED can be deleted from the department view"
            );

        }

        ticketRepository.delete(ticket);

    }

    /**
     * Deletes a ticket, but only if it is {@link Priority#LOW} priority —
     * this is the one deletion an admin is allowed to perform.
     *
     * @param ticketId the ticket to delete
     * @throws TicketNotFoundException if no ticket exists with the given id
     * @throws InvalidTicketException  if the ticket is not LOW priority
     */
    public void deleteLowPriorityTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (ticket.getPriority() != Priority.LOW) {

            throw new InvalidTicketException(
                    "Only LOW priority tickets can be deleted from the admin dashboard"
            );

        }

        ticketRepository.delete(ticket);

    }

private void validateTicketData(
        String message,
        User user,
        TicketRoutingResult result
) {


    if(message == null || message.isBlank()) {

        throw new InvalidTicketException(
                "Cannot save an empty ticket"
        );

    }


    if(user == null) {

        throw new InvalidTicketException(
                "User information is missing"
        );

    }


    if(result == null) {

        throw new InvalidTicketException(
                "Ticket routing result is missing"
        );

    }


    if(result.category() == null ||
       result.priority() == null ||
       result.assignedTeam() == null) {


        throw new InvalidTicketException(
                "Incomplete routing result received"
        );

    }

}

}
