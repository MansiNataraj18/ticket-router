package com.example.ticket_router.service;

import com.example.ticket_router.client.QdrantVectorClient;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QdrantService}, the validation/error-translation
 * layer over {@link QdrantVectorClient}.
 */
@ExtendWith(MockitoExtension.class)
class QdrantServiceTest {

    private static final List<Float> VECTOR = List.of(0.1f, 0.2f);

    @Mock
    private QdrantVectorClient qdrantVectorClient;

    private QdrantService qdrantService;

    @BeforeEach
    void setUp() {
        qdrantService = new QdrantService(qdrantVectorClient);
    }

    @Test
    void findSimilarTickets_throwsInvalidTicketException_whenVectorIsEmpty() {
        assertThrows(InvalidTicketException.class,
                () -> qdrantService.findSimilarTickets(Collections.emptyList()));
    }

    @Test
    void findSimilarTickets_returnsClientResult_whenFound() {
        when(qdrantVectorClient.searchSimilarTickets(VECTOR)).thenReturn("{\"result\":[]}");

        String result = qdrantService.findSimilarTickets(VECTOR);

        assertEquals("{\"result\":[]}", result);
    }

    @Test
    void findSimilarTickets_returnsFallbackMessage_whenClientReturnsBlank() {
        when(qdrantVectorClient.searchSimilarTickets(VECTOR)).thenReturn("   ");

        String result = qdrantService.findSimilarTickets(VECTOR);

        assertEquals("No similar historical tickets found.", result);
    }

    @Test
    void findSimilarTickets_wrapsFailure_inRoutingException() {
        when(qdrantVectorClient.searchSimilarTickets(VECTOR)).thenThrow(new RuntimeException("Qdrant is down"));

        assertThrows(RoutingException.class, () -> qdrantService.findSimilarTickets(VECTOR));
    }

    @Test
    void storeTicket_throwsInvalidTicketException_whenTicketTextIsBlank() {
        assertThrows(InvalidTicketException.class,
                () -> qdrantService.storeTicket("   ", VECTOR));
    }

    @Test
    void storeTicket_throwsInvalidTicketException_whenVectorIsEmpty() {
        assertThrows(InvalidTicketException.class,
                () -> qdrantService.storeTicket("some ticket", Collections.emptyList()));
    }

    @Test
    void storeTicket_callsClientWithGeneratedIdAndGivenData_whenValid() {
        qdrantService.storeTicket("some ticket", VECTOR);

        verify(qdrantVectorClient).saveTicket(anyString(), eq("some ticket"), eq(VECTOR));
    }

    @Test
    void storeTicket_wrapsFailure_inRoutingException() {
        doThrow(new RuntimeException("Qdrant write failed"))
                .when(qdrantVectorClient).saveTicket(anyString(), anyString(), eq(VECTOR));

        assertThrows(RoutingException.class, () -> qdrantService.storeTicket("some ticket", VECTOR));
    }
}
