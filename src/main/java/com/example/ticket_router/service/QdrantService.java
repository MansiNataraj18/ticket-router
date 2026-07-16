package com.example.ticket_router.service;

import com.example.ticket_router.client.QdrantVectorClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

/**
 * Validation/error-translation layer over {@link QdrantVectorClient}, used
 * to both find similar historical tickets (for RAG context) and persist new
 * ticket embeddings.
 */
@Service
public class QdrantService {

    private static final Logger log = LoggerFactory.getLogger(QdrantService.class);

    private final QdrantVectorClient qdrantVectorClient;


    /**
     * @param qdrantVectorClient the underlying Qdrant HTTP client
     */
    public QdrantService(QdrantVectorClient qdrantVectorClient) {
        this.qdrantVectorClient = qdrantVectorClient;
    }

    /**
     * Finds historical tickets similar to the given embedding vector, for use
     * as RAG context in ticket routing.
     *
     * @param vector the embedding vector to search against
     * @return the raw JSON search results from Qdrant, or a human-readable
     *         fallback message if no similar tickets were found
     * @throws InvalidTicketException if {@code vector} is null or empty
     * @throws RoutingException if the underlying Qdrant search call fails
     */
    public String findSimilarTickets(List<Float> vector) {


    if(vector == null || vector.isEmpty()) {

        throw new InvalidTicketException(
                "Cannot search Qdrant with an empty vector"
        );

    }


    try {

        String result =
                qdrantVectorClient.searchSimilarTickets(vector);



        if(result == null || result.isBlank()) {

            log.debug("No similar tickets found in Qdrant");

            return "No similar historical tickets found.";

        }

        return result;


    } catch(Exception e) {

        log.error("Qdrant similarity search failed: {}", e.getMessage(), e);

        throw new RoutingException(
                "Failed to search similar tickets in Qdrant",
                e
        );

    }

}

    /**
     * Stores a ticket's text and embedding vector in Qdrant, generating a new
     * random point ID for it.
     *
     * @param ticketText the ticket message to store as payload
     * @param vector     the embedding vector for {@code ticketText}
     * @throws InvalidTicketException if {@code ticketText} or {@code vector} is null/empty
     * @throws RoutingException if the underlying Qdrant write fails
     */
    public void storeTicket(
        String ticketText,
        List<Float> vector
) {


    if(ticketText == null || ticketText.isBlank()) {

        throw new InvalidTicketException(
                "Cannot store an empty ticket"
        );

    }


    if(vector == null || vector.isEmpty()) {

        throw new InvalidTicketException(
                "Cannot store ticket without embedding"
        );

    }


    try {

        String id =
                UUID.randomUUID().toString();


        qdrantVectorClient.saveTicket(
                id,
                ticketText,
                vector
        );

        log.debug("Stored ticket {} in Qdrant", id);


    } catch(Exception e) {

        log.error("Failed to store ticket in Qdrant: {}", e.getMessage(), e);

        throw new RoutingException(
                "Failed to store ticket in Qdrant",
                e
        );

    }

    }

}