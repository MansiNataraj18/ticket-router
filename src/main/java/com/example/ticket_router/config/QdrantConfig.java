package com.example.ticket_router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantConfig {

    @Bean
    public WebClient qdrantWebClient(QdrantProperties properties) {

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

}