package com.example.ticket_router.exception;

/**
 * Thrown when a step of the AI ticket-routing pipeline fails, such as an
 * embedding call, a Qdrant search/store operation, the LLM classification
 * call, or parsing the LLM's response. Translated to an HTTP 503 response by
 * {@link GlobalExceptionHandler}.
 */
public class RoutingException extends RuntimeException {
    public RoutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoutingException(String message) {
        super(message);
    }
}