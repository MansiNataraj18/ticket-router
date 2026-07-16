package com.example.ticket_router.service;

import com.example.ticket_router.client.OpenAiEmbeddingClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;

import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;

/**
 * Thin validation/error-translation layer over {@link OpenAiEmbeddingClient}.
 * <p>
 * Ensures callers never receive a null or empty embedding without an
 * explicit exception, and wraps any client-level failure in a
 * {@link RoutingException} so callers only need to handle one exception type.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final OpenAiEmbeddingClient embeddingClient;

    /**
     * @param embeddingClient the underlying OpenAI embeddings client
     */
    public EmbeddingService(OpenAiEmbeddingClient embeddingClient){
        this.embeddingClient = embeddingClient;
    }

    /**
     * Generates an embedding vector for the given text.
     *
     * @param text the text to embed; must not be null or blank
     * @return a non-empty embedding vector
     * @throws InvalidTicketException if {@code text} is null or blank
     * @throws RoutingException if the embedding client fails or returns an empty vector
     */
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