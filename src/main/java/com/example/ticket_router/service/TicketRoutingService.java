package com.example.ticket_router.service;

import com.example.ticket_router.client.TicketRoutingLlmClient;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class TicketRoutingService {

    private final TicketRoutingLlmClient llmClient;
    private final ObjectMapper objectMapper;

    public TicketRoutingService(
            TicketRoutingLlmClient llmClient,
            ObjectMapper objectMapper
    ) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public TicketRoutingResult route(String message) throws Exception {

        //calls llm client to route the ticket message and returns the raw JSON response
        String rawJson = llmClient.routeTicket(message);

        //parses the raw JSON response into a TicketRoutingResult object using the ObjectMapper
        return objectMapper.readValue(
                rawJson,
                TicketRoutingResult.class
        );
    }
}
