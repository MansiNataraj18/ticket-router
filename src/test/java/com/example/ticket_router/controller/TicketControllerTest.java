package com.example.ticket_router.controller;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketRequest;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.exception.UserNotFoundException;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TicketController}, the REST endpoint that
 * classifies a ticket via {@link TicketRoutingService} and, only when the
 * caller is authenticated, persists it via {@link TicketService}.
 */
@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private static final TicketRoutingResult RESULT =
            new TicketRoutingResult("Login issue", Priority.HIGH, "Engineering Department", "reasoning");

    @Mock
    private TicketRoutingService ticketRoutingService;

    @Mock
    private TicketService ticketService;

    @Mock
    private UserRepository userRepository;

    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        ticketController = new TicketController(ticketRoutingService, ticketService, userRepository);
    }

    @Test
    void route_returnsResult_withoutSaving_whenCallerIsAnonymous() {
        TicketRequest request = new TicketRequest("My login is broken");
        when(ticketRoutingService.route("My login is broken")).thenReturn(RESULT);

        TicketRoutingResult result = ticketController.route(request, null);

        assertEquals(RESULT, result);
        verifyNoInteractions(ticketService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void route_savesTicket_whenCallerIsAuthenticated() {
        TicketRequest request = new TicketRequest("My login is broken");
        User user = new User("bob", "hashed", "Bob", null);

        when(ticketRoutingService.route("My login is broken")).thenReturn(RESULT);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        TicketRoutingResult result = ticketController.route(
                request, new UsernamePasswordAuthenticationToken("bob", "n/a", List.<GrantedAuthority>of()));

        assertEquals(RESULT, result);
        verify(ticketService).saveTicket("My login is broken", user, RESULT);
    }

    @Test
    void route_throwsUserNotFoundException_whenAuthenticatedUsersAccountIsMissing() {
        TicketRequest request = new TicketRequest("My login is broken");
        when(ticketRoutingService.route("My login is broken")).thenReturn(RESULT);
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> ticketController.route(
                        request, new UsernamePasswordAuthenticationToken("ghost", "n/a", List.<GrantedAuthority>of())));
    }
}
