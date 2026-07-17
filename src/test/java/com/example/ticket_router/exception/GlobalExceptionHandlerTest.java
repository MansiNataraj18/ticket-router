package com.example.ticket_router.exception;

import com.example.ticket_router.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler}, verifying each custom
 * exception is translated into the right HTTP status and {@link ErrorResponse}.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/tickets/route");
    }

    @Test
    void handlesTicketNotFound_as404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTicketNotFound(new TicketNotFoundException(42L), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Ticket with id 42 not found", response.getBody().message());
        assertEquals("/api/tickets/route", response.getBody().path());
    }

    @Test
    void handlesUserNotFound_as404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(new UserNotFoundException("bob"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handlesInvalidTicket_as400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidTicket(new InvalidTicketException("bad ticket"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad ticket", response.getBody().message());
    }

    @Test
    void handlesRoutingFailure_as503() {
        ResponseEntity<ErrorResponse> response =
                handler.handleRoutingFailure(new RoutingException("routing failed"), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handlesUnexpectedException_as500_withGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneralException(new RuntimeException("something broke"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", response.getBody().message());
    }

    @Test
    void handlesValidationException_usingFirstFieldError() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("ticketRequest", "message", "This field cannot be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This field cannot be blank", response.getBody().message());
    }
}
