package com.example.ticket_router.client;

import org.springframework.stereotype.Component;
import com.example.ticket_router.prompt.TicketRoutingPrompt;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.List;

import com.example.ticket_router.exception.RoutingException;

/**
 * {@link TicketRoutingLlmClient} implementation backed by an OpenAI chat
 * completion model.
 * <p>
 * Sends the ticket message together with {@link TicketRoutingPrompt#SYSTEM_PROMPT}
 * as a JSON-mode chat request, and returns the raw JSON content of the
 * model's reply for the caller to parse into a {@link
 * com.example.ticket_router.dto.TicketRoutingResult}.
 */
@Component
public class OpenAiTicketRoutingClient implements TicketRoutingLlmClient {

    private final WebClient webClient;

    /** Used to parse the OpenAI chat completion response into a {@link JsonNode}. */
    private final ObjectMapper objectMapper;

    /**
     * @param openAiWebClient a {@link WebClient} pre-configured with the OpenAI
     *                         base URL and {@code Authorization} header
     * @param objectMapper    Jackson mapper used to read the raw API response
     */
    public OpenAiTicketRoutingClient(WebClient openAiWebClient, ObjectMapper objectMapper) {
        this.webClient = openAiWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Routes a ticket based on its message using the OpenAI chat completions API.
     *
     * @param ticketMessage the (optionally RAG-enriched) message of the ticket to route
     * @return the raw JSON routing decision returned by the model
     * @throws RoutingException if the OpenAI call fails or the response cannot be parsed
     */
   @Override
public String routeTicket(String ticketMessage) {

    try {

        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", TicketRoutingPrompt.SYSTEM_PROMPT
                        ),
                        Map.of(
                                "role", "user",
                                "content", ticketMessage
                        )
                ),
                "response_format",
                Map.of(
                        "type", "json_object"
                )
        );

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode json = objectMapper.readTree(response);

        return json
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();

    } catch (Exception e) {

        throw new RoutingException(
                "Failed to get routing decision from OpenAI",
                e
        );

    }
}
}
