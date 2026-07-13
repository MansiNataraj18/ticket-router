package com.example.ticket_router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

//used to bind the openai configuration properties from application.properties file into java object
@ConfigurationProperties(prefix = "openai")
//records contain getter methods, equals(), hashCode(), and toString() methods, and are immutable by default
public record OpenAiProperties (
    String apiKey,
    String baseUrl,
    String model
) {}
