package com.example.ticket_router.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the shared {@link ObjectMapper} bean used across the application
 * for JSON (de)serialization, such as parsing OpenAI API responses.
 */
@Configuration
public class JacksonConfig {

    /**
     * @return a default-configured Jackson {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
