package com.example.ticket_router.service;

import com.example.ticket_router.client.OpenAiEmbeddingClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;

import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final OpenAiEmbeddingClient embeddingClient;

    public EmbeddingService(OpenAiEmbeddingClient embeddingClient){
        this.embeddingClient = embeddingClient;
    }

    public List<Float> generate(String text){


    if(text == null || text.isBlank()) {

        throw new InvalidTicketException(
                "Cannot generate embedding for empty text"
        );

    }


    try {

        List<Float> vector =
                embeddingClient.createEmbedding(text);



        if(vector == null || vector.isEmpty()) {

            throw new RoutingException(
                    "Embedding service returned an empty vector"
            );

        }

        log.debug("Generated embedding of size {}", vector.size());

        return vector;


    } catch (RoutingException e) {

        throw e;


    } catch (Exception e) {

        log.error("Failed to generate embedding: {}", e.getMessage(), e);

        throw new RoutingException(
                "Failed to generate ticket embedding",
                e
        );

    }

}

}