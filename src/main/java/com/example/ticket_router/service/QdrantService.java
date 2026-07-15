package com.example.ticket_router.service;

import com.example.ticket_router.client.QdrantVectorClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

@Service
public class QdrantService {

    private static final Logger log = LoggerFactory.getLogger(QdrantService.class);

    private final QdrantVectorClient qdrantVectorClient;


    public QdrantService(QdrantVectorClient qdrantVectorClient) {
        this.qdrantVectorClient = qdrantVectorClient;
    }

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