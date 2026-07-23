package com.example.ticket_router.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * The outcome of routing a ticket through the AI pipeline: its classified
 * category, assigned {@link Priority}, the team it was routed to, and the
 * model's reasoning. Returned by {@code POST /api/tickets/route} and used to
 * persist the ticket via {@link com.example.ticket_router.entity.Ticket}.
 */
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
