package com.example.ticket_router.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * The JSON body submitted to {@code POST /api/tickets/route}, carrying the
 * raw support message text to be classified by the AI routing pipeline.
 */
public record TicketRequest (
    @NotBlank(message="This field cannot be blank")
    String message
) {}
