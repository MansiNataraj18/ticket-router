package com.example.ticket_router.controller;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.TicketService;
import com.example.ticket_router.dto.TicketRequest;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.entity.UserProfile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {


    private final TicketRoutingService service;

    private final TicketService ticketService;



    public TicketController(
            TicketRoutingService service,
            TicketService ticketService
    ) {

        this.service = service;
        this.ticketService = ticketService;

    }



    @PostMapping("/route")
    public TicketRoutingResult route(
            @Valid @RequestBody TicketRequest request,
            HttpSession session
    ) throws Exception {


        TicketRoutingResult result =
                service.route(
                        request.message()
                );



        UserProfile userProfile =
                (UserProfile)
                session.getAttribute(
                        "userProfile"
                );



        if (userProfile != null) {

            ticketService.saveTicket(
                    request.message(),
                    userProfile,
                    result
            );

        }



        return result;

    }

}