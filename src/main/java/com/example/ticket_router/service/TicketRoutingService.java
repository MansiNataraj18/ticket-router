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

/**
 * Orchestrates the end-to-end Retrieval-Augmented Generation (RAG) pipeline
 * used to classify a support ticket.
 * <p>
 * The pipeline is: validate the message, embed it ({@link EmbeddingService}),
 * search for similar historical tickets ({@link QdrantService}), build an
 * enriched prompt from both, send it to the LLM ({@link
 * TicketRoutingLlmClient}), and parse the JSON reply into a {@link
 * TicketRoutingResult}.
 */
@Service
public class TicketRoutingService {

    private static final Logger log = LoggerFactory.getLogger(TicketRoutingService.class);

    private final TicketRoutingLlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;


    /**
     * @param llmClient        performs the final classification call to the LLM
     * @param objectMapper     used to parse the LLM's JSON response into a {@link TicketRoutingResult}
     * @param embeddingService generates the embedding vector for the incoming ticket
     * @param qdrantService    finds similar historical tickets for RAG context
     */
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


    /**
     * Classifies a support ticket message using the RAG pipeline described in
     * the class-level documentation.
     *
     * @param message the raw ticket message text
     * @return the parsed category, priority, assigned team, and reasoning
     * @throws InvalidTicketException if {@code message} is null, blank, or
     *         over 5000 characters. Short or vague messages (e.g. "broken")
     *         are intentionally allowed through to the AI rather than
     *         rejected here — {@link com.example.ticket_router.prompt.TicketRoutingPrompt}
     *         instructs the model to classify them gracefully instead of
     *         refusing to respond.
     * @throws RoutingException if embedding generation, the Qdrant search, the
     *         LLM call, or parsing the LLM's response fails
     */
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


        if(message.length() > 5000) {

            throw new InvalidTicketException(
                    "Ticket message cannot exceed 5000 characters"
            );
        }

    }
}