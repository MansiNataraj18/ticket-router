package com.example.ticket_router.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(Long id){
        super("Ticket with id " + id + " not found");
    }
}