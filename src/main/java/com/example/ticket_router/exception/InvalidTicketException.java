package com.example.ticket_router.exception;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(String message){
        super(message);
    }
}