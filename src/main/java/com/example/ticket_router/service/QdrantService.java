package com.example.ticket_router.service;

import com.example.ticket_router.client.QdrantVectorClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class QdrantService {


    private final QdrantVectorClient qdrantVectorClient;


    public QdrantService(QdrantVectorClient qdrantVectorClient) {
        this.qdrantVectorClient = qdrantVectorClient;
    }

    public String findSimilarTickets(List<Float> vector) {

    return qdrantVectorClient.searchSimilarTickets(vector);

}
    public void storeTicket(
            String ticketText,
            List<Float> vector
    ) {

        String id = UUID.randomUUID().toString();

        qdrantVectorClient.saveTicket(
                id,
                ticketText,
                vector
        );

    }

}