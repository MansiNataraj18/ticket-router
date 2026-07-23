package com.example.ticket_router.dto;

/**
 * The AI-assigned severity of a ticket, ordered from least to most urgent.
 * Set by the routing pipeline and stored on {@link
 * com.example.ticket_router.entity.Ticket#getPriority()}.
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}