package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.exception.UserNotFoundException;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TicketPageController}, the "My Tickets" page.
 */
@ExtendWith(MockitoExtension.class)
class TicketPageControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private UserRepository userRepository;

    private TicketPageController ticketPageController;

    @BeforeEach
    void setUp() {
        ticketPageController = new TicketPageController(ticketService, userRepository);
    }

    @Test
    void myTickets_redirectsToLogin_whenNotAuthenticated() {
        String view = ticketPageController.myTickets(null, new ConcurrentModel());

        assertEquals("redirect:/login", view);
    }

    @Test
    void myTickets_returnsOwnTicketHistory_whenAuthenticated() {
        User user = new User("bob", "hashed", "Bob", null);
        List<Ticket> tickets = List.of(new Ticket());

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(ticketService.getTicketsForUser(user)).thenReturn(tickets);

        Model model = new ConcurrentModel();
        String view = ticketPageController.myTickets(authenticationWith("bob"), model);

        assertEquals("my-tickets", view);
        assertEquals("bob", model.asMap().get("userName"));
        assertEquals(tickets, model.asMap().get("tickets"));
    }

    @Test
    void myTickets_throwsUserNotFoundException_whenUserRecordIsMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> ticketPageController.myTickets(authenticationWith("ghost"), new ConcurrentModel()));
    }

    private static UsernamePasswordAuthenticationToken authenticationWith(String username, String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<>();
        for (String authority : authorities) {
            granted.add(new SimpleGrantedAuthority(authority));
        }
        return new UsernamePasswordAuthenticationToken(username, "n/a", granted);
    }
}
