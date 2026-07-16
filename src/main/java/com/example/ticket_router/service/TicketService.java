package com.example.ticket_router.service;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.exception.InvalidTicketException;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Persists submitted tickets and retrieves a given user's ticket history.
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
     * Saves a newly routed ticket against the given user profile.
     *
     * @param message     the original ticket message
     * @param userProfile the profile of the user who submitted the ticket
     * @param result      the AI-generated category, priority, team, and reasoning
     * @return the persisted {@link Ticket}, including its generated ID
     * @throws InvalidTicketException if {@code message} is blank, {@code userProfile}
     *         is null, or {@code result} is null/incomplete
     */
    public Ticket saveTicket(
        String message,
        UserProfile userProfile,
        TicketRoutingResult result
) {


    validateTicketData(
            message,
            userProfile,
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


        ticket.setUserProfile(
                userProfile
        );


        return ticketRepository.save(ticket);

    }

    /**
     * Retrieves a user's submitted tickets, most recent first.
     *
     * @param userProfile the profile to look up tickets for
     * @return the user's tickets ordered by creation date, descending
     */
    public List<Ticket> getTicketsForUser(
        UserProfile userProfile
) {

    return ticketRepository
            .findByUserProfileOrderByCreatedAtDesc(userProfile);

}
private void validateTicketData(
        String message,
        UserProfile userProfile,
        TicketRoutingResult result
) {


    if(message == null || message.isBlank()) {

        throw new InvalidTicketException(
                "Cannot save an empty ticket"
        );

    }


    if(userProfile == null) {

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