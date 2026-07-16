package com.example.ticket_router.service;

import com.example.ticket_router.client.TicketRoutingLlmClient;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.exception.InvalidTicketException;
import com.example.ticket_router.exception.RoutingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketRoutingService {

    private static final Logger log = LoggerFactory.getLogger(TicketRoutingService.class);

    private final TicketRoutingLlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;


    public TicketRoutingService(
            TicketRoutingLlmClient llmClient,
            ObjectMapper objectMapper,
            EmbeddingService embeddingService,
            QdrantService qdrantService
    ) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
    }


    public TicketRoutingResult route(String message) {


        // 1. Validate input
        validateTicket(message);

        log.debug("Routing ticket: {} characters", message.length());


        try {

            // 2. Generate embedding
            List<Float> vector =
                    embeddingService.generate(message);



            // 3. Search similar tickets
            String similarTickets =
                    qdrantService.findSimilarTickets(vector);

            log.debug("Found similar tickets context for routing");



            // 4. Create enriched prompt
            String enrichedMessage =
                    """
                    Similar historical tickets:

                    %s


                    New ticket:

                    %s
                    """.formatted(
                            similarTickets,
                            message
                    );



            // 5. Call LLM
            String rawJson =
                    llmClient.routeTicket(enrichedMessage);

            log.debug("Received LLM routing response");



            // 6. Parse response
            return objectMapper.readValue(
                    rawJson,
                    TicketRoutingResult.class
            );


        } catch (Exception e) {

            log.error("Ticket routing failed: {}", e.getMessage(), e);

            throw new RoutingException(
                    "Failed to process ticket routing",
                    e
            );

        }
    }



    private void validateTicket(String message) {


        if(message == null ||
                message.isBlank()) {

            throw new InvalidTicketException(
                    "Ticket message cannot be empty"
            );
        }


        if(message.length() < 10) {

            throw new InvalidTicketException(
                    "Ticket message must contain at least 10 characters"
            );
        }


        if(message.length() > 5000) {

            throw new InvalidTicketException(
                    "Ticket message cannot exceed 5000 characters"
            );
        }

    }
}