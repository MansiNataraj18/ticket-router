package com.example.ticket_router.controller;


import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.service.TicketService;
import com.example.ticket_router.service.UserProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class TicketPageController {

    private static final Logger log = LoggerFactory.getLogger(TicketPageController.class);

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

            log.debug("Unauthenticated access to '/my-tickets' - redirecting to login");

            return "redirect:/login";

        }

        log.info("User '{}' viewed their ticket history", authentication.getName());

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