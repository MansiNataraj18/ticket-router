package com.example.ticket_router.controller;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketStatus;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.repository.TicketRepository;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DepartmentController}, covering the "own department
 * only" scoping on both the dashboard listing and the per-ticket actions.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    private static final String ENGINEERING = "Engineering Department";
    private static final String ACCOUNTS = "Accounts Department";

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        departmentController = new DepartmentController(ticketService, ticketRepository, userRepository);
    }

    @Test
    void dashboard_redirectsHome_whenUserIsNotDepartmentStaff() {
        User customer = userOfType(userType("CUSTOMER", null), "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(customer));

        String view = departmentController.dashboard(authenticationWith("alice"), null, new ConcurrentModel());

        assertEquals("redirect:/", view);
    }

    @Test
    void dashboard_listsOwnDepartmentTickets() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        List<Ticket> tickets = List.of(new Ticket());

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketService.getTicketsForDepartment(ENGINEERING, null)).thenReturn(tickets);

        Model model = new ConcurrentModel();
        String view = departmentController.dashboard(authenticationWith("staff1"), null, model);

        assertEquals("department", view);
        assertEquals(ENGINEERING, model.asMap().get("departmentName"));
        assertEquals(tickets, model.asMap().get("tickets"));
        assertEquals(Boolean.TRUE, model.asMap().get("isDepartmentStaff"));
        assertEquals(Boolean.FALSE, model.asMap().get("isAdmin"));
    }

    @Test
    void dashboard_appliesPriorityFilter_whenPriorityParamGiven() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        List<Ticket> filtered = List.of(new Ticket());

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketService.getTicketsForDepartment(ENGINEERING, Priority.HIGH)).thenReturn(filtered);

        Model model = new ConcurrentModel();
        departmentController.dashboard(authenticationWith("staff1"), "high", model);

        assertEquals("HIGH", model.asMap().get("selectedPriority"));
        assertEquals(filtered, model.asMap().get("tickets"));
    }

    @Test
    void updateStatus_updatesTicket_whenItBelongsToCallersDepartment() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        Ticket ticket = new Ticket();
        ticket.setAssignedTeam(ENGINEERING);

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        String view = departmentController.updateStatus(5L, "completed", authenticationWith("staff1"));

        assertEquals("redirect:/department", view);
        verify(ticketService).updateStatus(5L, TicketStatus.COMPLETED);
    }

    @Test
    void updateStatus_ignoresInvalidStatusValue() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        Ticket ticket = new Ticket();
        ticket.setAssignedTeam(ENGINEERING);

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        String view = departmentController.updateStatus(5L, "not-a-status", authenticationWith("staff1"));

        assertEquals("redirect:/department", view);
        verify(ticketService, never()).updateStatus(anyLong(), any());
    }

    @Test
    void updateStatus_throwsInvalidTicketException_whenTicketBelongsToAnotherDepartment() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        Ticket ticket = new Ticket();
        ticket.setAssignedTeam(ACCOUNTS);

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(9L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketException.class,
                () -> departmentController.updateStatus(9L, "completed", authenticationWith("staff1")));
    }

    @Test
    void deleteTicket_deletesTicket_whenItBelongsToCallersDepartment() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        Ticket ticket = new Ticket();
        ticket.setAssignedTeam(ENGINEERING);

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(3L)).thenReturn(Optional.of(ticket));

        String view = departmentController.deleteTicket(3L, authenticationWith("staff1"));

        assertEquals("redirect:/department", view);
        verify(ticketService).deleteRejectedTicket(3L);
    }

    @Test
    void deleteTicket_throwsInvalidTicketException_whenTicketBelongsToAnotherDepartment() {
        User staff = userOfType(userType("ENGINEERING_STAFF", ENGINEERING), "staff1");
        Ticket ticket = new Ticket();
        ticket.setAssignedTeam(ACCOUNTS);

        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(ticketRepository.findById(4L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketException.class,
                () -> departmentController.deleteTicket(4L, authenticationWith("staff1")));
    }

    private static UserType userType(String name, String departmentTeamName) {
        UserType userType = new UserType();
        ReflectionTestUtils.setField(userType, "name", name);
        ReflectionTestUtils.setField(userType, "departmentTeamName", departmentTeamName);
        return userType;
    }

    private static User userOfType(UserType userType, String username) {
        return new User(username, "hashed", username, userType);
    }

    private static UsernamePasswordAuthenticationToken authenticationWith(String username, String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<>();
        for (String authority : authorities) {
            granted.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(authority));
        }
        return new UsernamePasswordAuthenticationToken(username, "n/a", granted);
    }
}
