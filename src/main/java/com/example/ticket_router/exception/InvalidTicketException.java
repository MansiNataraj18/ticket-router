package com.example.ticket_router.exception;

/**
 * Thrown when ticket-related input fails validation, e.g. an empty or
 * over-length ticket message, a missing user, or an incomplete AI routing
 * result. Translated to an HTTP 400 response by {@link GlobalExceptionHandler}.
 */
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message){
        super(message);
    }
}