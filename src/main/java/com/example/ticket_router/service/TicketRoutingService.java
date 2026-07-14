package com.example.ticket_router.service;

import com.example.ticket_router.client.TicketRoutingLlmClient;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketRoutingService {

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


    public TicketRoutingResult route(String message) throws Exception {


        // 1. Create embedding for incoming ticket
        List<Float> vector =
                embeddingService.generate(message);


        // 2. Search similar historical tickets
        String similarTickets =
                qdrantService.findSimilarTickets(vector);


        // 3. Add retrieved context to prompt
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


        // 4. Send enriched ticket to OpenAI
        String rawJson =
                llmClient.routeTicket(enrichedMessage);


        // 5. Convert JSON response to Java object
        return objectMapper.readValue(
                rawJson,
                TicketRoutingResult.class
        );
    }
}