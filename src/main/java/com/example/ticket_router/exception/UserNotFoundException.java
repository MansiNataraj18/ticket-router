package com.example.ticket_router.exception;

/**
 * Thrown when a user lookup by id or username finds no matching record.
 * Translated to an HTTP 404 response by {@link GlobalExceptionHandler}.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id){
        super("User with id " + id + " not found");
    }

    public UserNotFoundException(String username){
        super("User with username '" + username + "' not found");
    }
}