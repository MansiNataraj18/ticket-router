package com.example.ticket_router.exception;

/**
 * Thrown when a ticket lookup by id finds no matching record. Translated to
 * an HTTP 404 response by {@link GlobalExceptionHandler}.
 */
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long id){
        super("Ticket with id " + id + " not found");
    }
}