package com.example.ticket_router.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the {@link WebClient} used to call the OpenAI API, wiring in
 * the base URL and API key from {@link OpenAiProperties}.
 */
@Configuration
public class OpenAiConfig {

    /**
     * @param openAiProperties the configured OpenAI base URL and API key
     * @return a {@link WebClient} pre-configured with OpenAI's base URL and
     *         {@code Authorization} bearer header
     */
    @Bean
    public WebClient openAiWebClient(OpenAiProperties openAiProperties) {
        return WebClient.builder()
                .baseUrl(openAiProperties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + openAiProperties.apiKey())
                .build();
    }
}
