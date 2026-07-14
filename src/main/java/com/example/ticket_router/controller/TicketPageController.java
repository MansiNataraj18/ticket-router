package com.example.ticket_router.controller;


import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.service.TicketService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class TicketPageController {


    private final TicketService ticketService;


    public TicketPageController(
            TicketService ticketService
    ) {

        this.ticketService = ticketService;

    }



    @GetMapping("/my-tickets")
    public String myTickets(
            HttpSession session,
            Model model
    ) {


        UserProfile userProfile =
                (UserProfile)
                session.getAttribute(
                        "userProfile"
                );


        if (userProfile == null) {

            return "redirect:/login";

        }



        List<Ticket> tickets =
                ticketService
                        .getTicketsForUser(
                                userProfile
                        );



        model.addAttribute(
                "tickets",
                tickets
        );


        return "my-tickets";

    }

}