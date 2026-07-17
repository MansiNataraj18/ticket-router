package com.example.ticket_router.service;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.dto.TicketStatus;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.TicketNotFoundException;
import com.example.ticket_router.repository.TicketRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TicketService}.
 * <p>
 * Covers the validation rules in {@code saveTicket}, and the two
 * conditional-delete rules ("only REJECTED tickets can be deleted by a
 * department" and "only LOW priority tickets can be deleted by admin") that
 * are the core business logic of this class. The repository is mocked, so
 * these run without a database.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private User user;
    private TicketRoutingResult validResult;

    @BeforeEach
    void setUp() {

        user = new User("bob", "hashed-password", "Bob Smith", null);

        validResult = new TicketRoutingResult(
                "Login issue",
                Priority.HIGH,
                "Engineering Department",
                "The user cannot log in, which matches past outage tickets."
        );

        lenient().when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void saveTicket_savesFieldsFromRoutingResult_whenInputIsValid() {

        Ticket saved = ticketService.saveTicket("I can't log in", user, validResult);

        assertEquals("I can't log in", saved.getMessage());
        assertEquals("Login issue", saved.getCategory());
        assertEquals(Priority.HIGH, saved.getPriority());
        assertEquals("Engineering Department", saved.getAssignedTeam());
        assertEquals(user, saved.getUser());
        assertEquals(TicketStatus.PENDING, saved.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void saveTicket_throwsInvalidTicketException_whenMessageIsBlank() {

        assertThrows(InvalidTicketException.class,
                () -> ticketService.saveTicket("   ", user, validResult));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void saveTicket_throwsInvalidTicketException_whenUserIsNull() {

        assertThrows(InvalidTicketException.class,
                () -> ticketService.saveTicket("message", null, validResult));
    }

    @Test
    void saveTicket_throwsInvalidTicketException_whenRoutingResultIsIncomplete() {

        TicketRoutingResult incomplete =
                new TicketRoutingResult(null, Priority.LOW, "Support Team", "reasoning");

        assertThrows(InvalidTicketException.class,
                () -> ticketService.saveTicket("message", user, incomplete));
    }

    @Test
    void deleteLowPriorityTicket_deletesTicket_whenPriorityIsLow() {

        Ticket ticket = new Ticket();
        ticket.setPriority(Priority.LOW);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        ticketService.deleteLowPriorityTicket(1L);

        verify(ticketRepository).delete(ticket);
    }

    @Test
    void deleteLowPriorityTicket_throwsInvalidTicketException_whenPriorityIsNotLow() {

        Ticket ticket = new Ticket();
        ticket.setPriority(Priority.HIGH);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketException.class,
                () -> ticketService.deleteLowPriorityTicket(1L));

        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void deleteLowPriorityTicket_throwsTicketNotFoundException_whenTicketDoesNotExist() {

        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.deleteLowPriorityTicket(99L));
    }

    @Test
    void deleteRejectedTicket_deletesTicket_whenStatusIsRejected() {

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.REJECTED);
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket));

        ticketService.deleteRejectedTicket(2L);

        verify(ticketRepository).delete(ticket);
    }

    @Test
    void deleteRejectedTicket_throwsInvalidTicketException_whenStatusIsNotRejected() {

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketException.class,
                () -> ticketService.deleteRejectedTicket(2L));

        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void updateStatus_updatesAndSavesTheTicket() {

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PENDING);
        when(ticketRepository.findById(3L)).thenReturn(Optional.of(ticket));

        Ticket updated = ticketService.updateStatus(3L, TicketStatus.COMPLETED);

        assertEquals(TicketStatus.COMPLETED, updated.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void updateStatus_throwsTicketNotFoundException_whenTicketDoesNotExist() {

        when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.updateStatus(404L, TicketStatus.COMPLETED));
    }
}
