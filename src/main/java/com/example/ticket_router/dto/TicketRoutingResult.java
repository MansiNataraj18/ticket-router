package com.example.ticket_router.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//record used to store immutable data for the ticket routing result
public record TicketRoutingResult(
    @NotBlank
    String category,
    //@NotNull is the NotBlank equivalent for enums, it ensures that the priority field is not null
    @NotNull
    Priority priority,
    @NotBlank
    String assignedTeam,
    @NotBlank
    String reasoning
) {}
