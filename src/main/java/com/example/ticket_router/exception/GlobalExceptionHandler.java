package com.example.ticket_router.exception;


import com.example.ticket_router.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFound(
            TicketNotFoundException ex,
            HttpServletRequest request
    ){

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        404,
                        "Ticket Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }



    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ){

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        404,
                        "User Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }




    @ExceptionHandler(InvalidTicketException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTicket(
            InvalidTicketException ex,
            HttpServletRequest request
    ){

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        400,
                        "Invalid Request",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(error);
    }




    @ExceptionHandler(RoutingException.class)
    public ResponseEntity<ErrorResponse> handleRoutingFailure(
            RoutingException ex,
            HttpServletRequest request
    ){

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        503,
                        "Routing Service Failed",
                        ex.getMessage(),
                        request.getRequestURI()
                );


        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);

    }





    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ){

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        500,
                        "Internal Server Error",
                        "Something went wrong",
                        request.getRequestURI()
                );


        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);

    }

}