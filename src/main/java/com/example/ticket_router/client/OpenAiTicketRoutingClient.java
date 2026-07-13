package com.example.ticket_router.client;

import org.springframework.stereotype.Component;
import com.example.ticket_router.prompt.TicketRoutingPrompt;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.List;

//registers this class as a Spring Component, allowing it to create beans
@Component
public class OpenAiTicketRoutingClient implements TicketRoutingLlmClient {

    private final WebClient webClient;
    //objectMapper is a class from the Jackson library that provides functionality for converting between Java objects and JSON. It is used to parse the JSON response from the OpenAI API into a JsonNode object, which can then be traversed to extract the relevant information.
    private final ObjectMapper objectMapper;

    //dependency injection by injecting construstor parameters
    public OpenAiTicketRoutingClient(WebClient openAiWebClient, ObjectMapper objectMapper) {
        this.webClient = openAiWebClient;
        this.objectMapper = objectMapper;
    }

    //implements the routeTicket method from the TicketRoutingLlmClient interface, which takes a ticket message as input and returns a routing decision as output
    @Override
    public String routeTicket(String ticketMessage) {
        //constructs the request body for the OpenAI API call, including the model, system

        //creates json sent to OpenAI 
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
                    //tells OpenAi tp return the response in JSON format only
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


            try {
                JsonNode json = objectMapper.readTree(response);

                return json
                        .get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText();
            } catch (Exception e) {
                throw new RuntimeException("Error parsing OpenAI response", e);
            }
        }
}
