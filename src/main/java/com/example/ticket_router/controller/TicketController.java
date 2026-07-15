package com.example.ticket_router.controller;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.TicketService;
import com.example.ticket_router.service.UserProfileService;
import com.example.ticket_router.dto.TicketRequest;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.entity.UserProfile;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {


    private final TicketRoutingService service;

    private final TicketService ticketService;

    private final UserProfileService userProfileService;



    public TicketController(
            TicketRoutingService service,
            TicketService ticketService,
            UserProfileService userProfileService
    ) {

        this.service = service;
        this.ticketService = ticketService;
        this.userProfileService = userProfileService;

    }



    @PostMapping("/route")
    public TicketRoutingResult route(
            @Valid @RequestBody TicketRequest request,
            Authentication authentication
    )  {


        TicketRoutingResult result =
                service.route(
                        request.message()
                );


        if (authentication != null && authentication.isAuthenticated()) {

            UserProfile userProfile =
                    userProfileService.getOrCreate(
                            authentication.getName()
                    );

            ticketService.saveTicket(
                    request.message(),
                    userProfile,
                    result
            );

        }


        return result;

    }

}