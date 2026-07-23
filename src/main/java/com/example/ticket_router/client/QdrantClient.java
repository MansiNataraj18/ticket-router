package com.example.ticket_router.client;

/**
 * Abstraction over the Qdrant vector database operations this application needs.
 * <p>
 * Implemented by {@link QdrantVectorClient}, which talks to Qdrant over HTTP.
 */
public interface QdrantClient {

    /**
     * Creates the Qdrant collection used to store ticket embeddings, if it
     * does not already exist. Safe to call on every application startup.
     */
    void createCollection();
}
