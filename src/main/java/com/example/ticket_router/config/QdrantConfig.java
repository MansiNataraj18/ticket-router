package com.example.ticket_router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the {@link WebClient} used to call the Qdrant vector database's
 * HTTP API, wiring in the base URL from {@link QdrantProperties}.
 */
@Configuration
public class QdrantConfig {

    /**
     * @param properties the configured Qdrant base URL/collection name
     * @return a {@link WebClient} pre-configured with Qdrant's base URL
     */
    @Bean
    public WebClient qdrantWebClient(QdrantProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
