package com.example.ticket_router.service;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.exception.InvalidTicketException;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TicketService {


    private final TicketRepository ticketRepository;


    public TicketService(
            TicketRepository ticketRepository
    ) {

        this.ticketRepository = ticketRepository;

    }


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