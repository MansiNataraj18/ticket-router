package com.example.ticket_router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Qdrant integration, bound from the
 * {@code qdrant.*} keys in the application configuration (base URL and the
 * name of the collection used to store ticket embeddings).
 */
@ConfigurationProperties(prefix = "qdrant")
public class QdrantProperties {

    private String baseUrl;
    private String collection;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
