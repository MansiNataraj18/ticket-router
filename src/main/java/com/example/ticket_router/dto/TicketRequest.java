package com.example.ticket_router.dto;

import jakarta.validation.constraints.NotBlank;

//record used to store immutable data for the ticket request
public record TicketRequest (
    @NotBlank(message="This field cannot be blank")
    String message
) {} 

