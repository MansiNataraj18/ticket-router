package com.example.ticket_router.controller;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminController}: the permission gate, the
 * priority filter/sort behavior, and the LOW-priority-only delete rule
 * (which itself is enforced by {@link TicketService}, mocked here).
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketService ticketService;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(ticketRepository, ticketService);
    }

    @Test
    void adminDashboard_redirectsHome_whenCallerLacksViewAllTicketsPermission() {
        String view = adminController.adminDashboard(
                authenticationWith("bob", "VIEW_DEPARTMENT_TICKETS"), null, null, new ConcurrentModel());

        assertEquals("redirect:/", view);
    }

    @Test
    void adminDashboard_listsAllTickets_newestFirst_whenNoFilterGiven() {
        Ticket older = ticketWithCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        Ticket newer = ticketWithCreatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        when(ticketRepository.findAll()).thenReturn(List.of(older, newer));

        Model model = new ConcurrentModel();
        String view = adminController.adminDashboard(
                authenticationWith("admin", "VIEW_ALL_TICKETS"), null, null, model);

        assertEquals("admin", view);
        assertEquals(List.of(newer, older), model.asMap().get("tickets"));
    }

    @Test
    void adminDashboard_filtersByPriority_whenPriorityParamGiven() {
        Ticket lowTicket = new Ticket();
        lowTicket.setPriority(Priority.LOW);
        when(ticketRepository.findByPriority(Priority.LOW)).thenReturn(List.of(lowTicket));

        Model model = new ConcurrentModel();
        String view = adminController.adminDashboard(
                authenticationWith("admin", "VIEW_ALL_TICKETS"), "low", null, model);

        assertEquals("admin", view);
        assertEquals("LOW", model.asMap().get("selectedPriority"));
        assertEquals(List.of(lowTicket), model.asMap().get("tickets"));
    }

    @Test
    void adminDashboard_ignoresUnrecognizedPriorityParam() {
        when(ticketRepository.findAll()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        adminController.adminDashboard(authenticationWith("admin", "VIEW_ALL_TICKETS"), "urgent!!", null, model);

        assertNull(model.asMap().get("selectedPriority"));
    }

    @Test
    void adminDashboard_sortsByPriorityDescending_whenSortParamIsPriority() {
        Ticket low = new Ticket();
        low.setPriority(Priority.LOW);
        Ticket high = new Ticket();
        high.setPriority(Priority.HIGH);
        Ticket medium = new Ticket();
        medium.setPriority(Priority.MEDIUM);
        when(ticketRepository.findAll()).thenReturn(List.of(low, high, medium));

        Model model = new ConcurrentModel();
        adminController.adminDashboard(authenticationWith("admin", "VIEW_ALL_TICKETS"), null, "priority", model);

        assertEquals(List.of(high, medium, low), model.asMap().get("tickets"));
        assertEquals("priority", model.asMap().get("selectedSort"));
    }

    @Test
    void deleteLowPriorityTicket_deletesAndRedirects_whenCallerHasPermission() {
        String view = adminController.deleteLowPriorityTicket(7L, authenticationWith("admin", "VIEW_ALL_TICKETS"));

        assertEquals("redirect:/admin", view);
        verify(ticketService).deleteLowPriorityTicket(7L);
    }

    @Test
    void deleteLowPriorityTicket_redirectsHome_withoutDeleting_whenCallerLacksPermission() {
        String view = adminController.deleteLowPriorityTicket(7L, authenticationWith("bob", "VIEW_DEPARTMENT_TICKETS"));

        assertEquals("redirect:/", view);
        verify(ticketService, never()).deleteLowPriorityTicket(anyLong());
    }

    private static Ticket ticketWithCreatedAt(Instant createdAt) {
        Ticket ticket = new Ticket();
        ReflectionTestUtils.setField(ticket, "createdAt", createdAt);
        return ticket;
    }

    private static UsernamePasswordAuthenticationToken authenticationWith(String username, String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<>();
        for (String authority : authorities) {
            granted.add(new SimpleGrantedAuthority(authority));
        }
        return new UsernamePasswordAuthenticationToken(username, "n/a", granted);
    }
}
