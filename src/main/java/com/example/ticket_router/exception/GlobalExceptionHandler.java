package com.example.ticket_router.exception;

import com.example.ticket_router.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

/**
 * Centralized exception-to-HTTP-response mapping for the whole application.
 * Catches the app's custom exceptions (and validation/general failures) and
 * converts them into a consistent {@link ErrorResponse} body with an
 * appropriate HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link TicketNotFoundException}, returning HTTP 404.
     *
     * @param ex      the exception raised for the missing ticket
     * @param request the failed request, used to populate the error path
     * @return a 404 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFound(
            TicketNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Ticket not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Ticket Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Handles {@link UserNotFoundException}, returning HTTP 404.
     *
     * @param ex      the exception raised for the missing user
     * @param request the failed request, used to populate the error path
     * @return a 404 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("User not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Handles {@link InvalidTicketException}, returning HTTP 400.
     *
     * @param ex      the exception raised for the invalid ticket input
     * @param request the failed request, used to populate the error path
     * @return a 400 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(InvalidTicketException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTicket(
            InvalidTicketException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid ticket request on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .badRequest()
                .body(error);
    }

    /**
     * Handles bean-validation failures on {@code @Valid} request bodies,
     * returning HTTP 400 with the first field error's message.
     *
     * @param ex      the exception carrying the binding/validation errors
     * @param request the failed request, used to populate the error path
     * @return a 400 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");
        log.warn("Validation failed on {}: {}", request.getRequestURI(), message);
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI()
        );
        return ResponseEntity
                .badRequest()
                .body(error);
    }

    /**
     * Handles {@link RoutingException}, returning HTTP 503 since it
     * indicates a failure in an external step of the routing pipeline
     * (embedding, Qdrant, or the LLM call).
     *
     * @param ex      the exception raised for the routing failure
     * @param request the failed request, used to populate the error path
     * @return a 503 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(RoutingException.class)
    public ResponseEntity<ErrorResponse> handleRoutingFailure(
            RoutingException ex,
            HttpServletRequest request
    ) {
        log.error("Routing service failure on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Routing Service Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    /**
     * Catch-all fallback for any exception not handled by a more specific
     * handler above, returning a generic HTTP 500 without leaking internal
     * details.
     *
     * @param ex      the unhandled exception
     * @param request the failed request, used to populate the error path
     * @return a 500 response with an {@link ErrorResponse} body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
