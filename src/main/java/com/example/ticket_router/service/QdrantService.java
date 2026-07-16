package com.example.ticket_router.service;

import com.example.ticket_router.client.QdrantVectorClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

@Service
public class QdrantService {


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

            return "No similar historical tickets found.";

        }


        return result;


    } catch(Exception e) {

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


    } catch(Exception e) {


        throw new RoutingException(
                "Failed to store ticket in Qdrant",
                e
        );

    }

    }

}