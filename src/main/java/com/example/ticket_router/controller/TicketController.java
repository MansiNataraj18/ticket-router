package com.example.ticket_router.controller;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.dto.TicketRequest;
import com.example.ticket_router.dto.TicketRoutingResult;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRoutingService service;

    public TicketController(TicketRoutingService service) {
        this.service = service;
    }

    @PostMapping("/route")
    public TicketRoutingResult route(
            @Valid @RequestBody TicketRequest request
    ) throws Exception {
        return service.route(request.message());
    }
}