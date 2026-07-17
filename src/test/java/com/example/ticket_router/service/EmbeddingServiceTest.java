package com.example.ticket_router.service;

import com.example.ticket_router.client.OpenAiEmbeddingClient;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EmbeddingService}, the thin validation/error
 * translation layer over {@link OpenAiEmbeddingClient}.
 */
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private OpenAiEmbeddingClient embeddingClient;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingClient);
    }

    @Test
    void generate_throwsInvalidTicketException_whenTextIsBlank() {
        assertThrows(InvalidTicketException.class, () -> embeddingService.generate("   "));
    }

    @Test
    void generate_returnsVector_whenClientSucceeds() {
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        when(embeddingClient.createEmbedding("hello")).thenReturn(vector);

        List<Float> result = embeddingService.generate("hello");

        assertEquals(vector, result);
    }

    @Test
    void generate_throwsRoutingException_whenClientReturnsEmptyVector() {
        when(embeddingClient.createEmbedding("hello")).thenReturn(Collections.emptyList());

        assertThrows(RoutingException.class, () -> embeddingService.generate("hello"));
    }

    @Test
    void generate_throwsRoutingException_whenClientReturnsNull() {
        when(embeddingClient.createEmbedding("hello")).thenReturn(null);

        assertThrows(RoutingException.class, () -> embeddingService.generate("hello"));
    }

    @Test
    void generate_wrapsUnexpectedFailure_inRoutingException() {
        when(embeddingClient.createEmbedding("hello")).thenThrow(new RuntimeException("OpenAI timeout"));

        assertThrows(RoutingException.class, () -> embeddingService.generate("hello"));
    }
}
