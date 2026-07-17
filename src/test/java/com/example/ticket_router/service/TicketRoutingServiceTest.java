package com.example.ticket_router.service;

import com.example.ticket_router.client.TicketRoutingLlmClient;
import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TicketRoutingService}, the RAG pipeline that turns a
 * raw ticket message into a category/priority/team classification.
 * <p>
 * {@link TicketRoutingLlmClient}, {@link EmbeddingService}, and
 * {@link QdrantService} are all mocked; only {@link ObjectMapper} is real, so
 * the JSON-parsing step is genuinely exercised rather than assumed.
 */
@ExtendWith(MockitoExtension.class)
class TicketRoutingServiceTest {

    private static final String VALID_MESSAGE = "The login page keeps returning a 500 error.";

    @Mock
    private TicketRoutingLlmClient llmClient;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private QdrantService qdrantService;

    private TicketRoutingService ticketRoutingService;

    @BeforeEach
    void setUp() {

        ticketRoutingService = new TicketRoutingService(
                llmClient,
                new ObjectMapper(),
                embeddingService,
                qdrantService
        );
    }

    @Test
    void route_returnsParsedResult_whenPipelineSucceeds() {

        when(embeddingService.generate(VALID_MESSAGE)).thenReturn(List.of(0.1f, 0.2f, 0.3f));
        when(qdrantService.findSimilarTickets(anyList())).thenReturn("Ticket: cannot access dashboard");
        when(llmClient.routeTicket(anyString())).thenReturn(
                "{\"category\":\"Authentication\",\"priority\":\"HIGH\","
                        + "\"assignedTeam\":\"Engineering Department\","
                        + "\"reasoning\":\"Matches a recent login outage.\"}"
        );

        TicketRoutingResult result = ticketRoutingService.route(VALID_MESSAGE);

        assertEquals("Authentication", result.category());
        assertEquals(Priority.HIGH, result.priority());
        assertEquals("Engineering Department", result.assignedTeam());

        verify(llmClient).routeTicket(argThat(prompt ->
                prompt.contains(VALID_MESSAGE) && prompt.contains("cannot access dashboard")));
    }

    @Test
    void route_throwsInvalidTicketException_whenMessageIsBlank() {

        assertThrows(InvalidTicketException.class, () -> ticketRoutingService.route("   "));

        verifyNoInteractions(embeddingService, qdrantService, llmClient);
    }

    @Test
    void route_throwsInvalidTicketException_whenMessageIsTooShort() {

        assertThrows(InvalidTicketException.class, () -> ticketRoutingService.route("too short"));
    }

    @Test
    void route_throwsInvalidTicketException_whenMessageIsTooLong() {

        String tooLong = "a".repeat(5001);

        assertThrows(InvalidTicketException.class, () -> ticketRoutingService.route(tooLong));
    }

    @Test
    void route_wrapsFailureInRoutingException_whenEmbeddingGenerationFails() {

        when(embeddingService.generate(VALID_MESSAGE)).thenThrow(new RuntimeException("OpenAI is down"));

        assertThrows(RoutingException.class, () -> ticketRoutingService.route(VALID_MESSAGE));
    }

    @Test
    void route_wrapsFailureInRoutingException_whenLlmReturnsUnparseableJson() {

        when(embeddingService.generate(VALID_MESSAGE)).thenReturn(List.of(0.1f));
        when(qdrantService.findSimilarTickets(anyList())).thenReturn("No similar historical tickets found.");
        when(llmClient.routeTicket(anyString())).thenReturn("this is not json");

        assertThrows(RoutingException.class, () -> ticketRoutingService.route(VALID_MESSAGE));
    }
}
