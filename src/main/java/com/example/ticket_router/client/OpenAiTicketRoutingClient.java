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
    /**
     * Routes a ticket based on its message using the OpenAI API.
     *
     * @param ticketMessage the message of the ticket to route
     * @return the routing decision for the ticket
     */
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

            //Used to create a POST request to the OpenAI API's chat completions endpoint, sending the request body and retrieving the response as a String. The block() method is used to block the execution until the response is received.
            String response = webClient.post()
            //specifies the endpoint for the OpenAI API's chat completions feature
                    .uri("/chat/completions")
                    //converts java map into json automatically
                    .bodyValue(request)
                    //sends the request
                    .retrieve()
                    //response body should be converted to a string
                    .bodyToMono(String.class)
                    //makes request synchronous 
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
