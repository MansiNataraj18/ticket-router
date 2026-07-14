package com.example.ticket_router.service;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.repository.TicketRepository;

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

}