package com.example.ticket_router.controller;


import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.service.TicketService;
import com.example.ticket_router.service.UserProfileService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class TicketPageController {


    private final TicketService ticketService;
    private final UserProfileService userProfileService;


    public TicketPageController(
            TicketService ticketService,
            UserProfileService userProfileService
    ) {

        this.ticketService = ticketService;
        this.userProfileService = userProfileService;

    }



    @GetMapping("/my-tickets")
    public String myTickets(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {

            return "redirect:/login";

        }

        UserProfile userProfile =
                userProfileService.getOrCreate(authentication.getName());



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