package com.example.ticket_router.dto;

/**
 * The department-facing workflow status of a ticket, separate from its
 * AI-assigned priority. Every new ticket starts as {@link #PENDING}; the
 * owning department's staff move it through the remaining states from
 * their department dashboard.
 */
public enum TicketStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED
}
