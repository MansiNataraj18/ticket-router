package com.example.ticket_router.dto;

import java.time.LocalDateTime;

/**
 * Standard JSON error body returned by {@link
 * com.example.ticket_router.exception.GlobalExceptionHandler} for every
 * handled exception, carrying the failure time, HTTP status, a short error
 * label, a human-readable message, and the request path that failed.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}