package com.example.ticket_router.client;

/**
 * Abstraction over the LLM call that classifies a support ticket.
 * <p>
 * Implemented by {@link OpenAiTicketRoutingClient}, which sends the ticket
 * message (enriched with similar historical tickets) to an OpenAI chat model
 * and expects a strict JSON response back.
 */
public interface TicketRoutingLlmClient {

    /**
     * Sends the given ticket message to the underlying LLM and returns its
     * raw JSON classification response.
     *
     * @param ticketMessage the (optionally RAG-enriched) ticket text to classify
     * @return the raw JSON string returned by the LLM, expected to match
     *         {@link com.example.ticket_router.dto.TicketRoutingResult}
     */
    String routeTicket(String ticketMessage);
}
