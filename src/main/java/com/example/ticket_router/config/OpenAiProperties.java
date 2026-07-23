package com.example.ticket_router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the OpenAI integration, bound from the
 * {@code openai.*} keys in the application configuration (API key, base
 * URL, and chat/embedding model to use).
 */
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties (
    String apiKey,
    String baseUrl,
    String model
) {}
