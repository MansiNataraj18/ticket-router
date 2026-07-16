package com.example.ticket_router.client;


import com.example.ticket_router.config.QdrantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
//import java.util.Map;

/**
 * {@link QdrantClient} implementation that talks to a Qdrant instance over
 * its HTTP API using a {@link WebClient} configured with the Qdrant base URL
 * (see {@link com.example.ticket_router.config.QdrantConfig}).
 * <p>
 * Tickets are stored as 1536-dimension vectors (matching OpenAI's
 * {@code text-embedding-3-small} model) with cosine distance, and each point
 * carries the original ticket text as payload for display in search results.
 */
@Component
public class QdrantVectorClient implements QdrantClient {


    private final WebClient webClient;
    private final QdrantProperties properties;


    /**
     * @param qdrantWebClient a {@link WebClient} pre-configured with the Qdrant base URL
     * @param properties      the configured Qdrant base URL/collection name
     */
    public QdrantVectorClient(
            WebClient qdrantWebClient,
            QdrantProperties properties
    ) {

        this.webClient = qdrantWebClient;
        this.properties = properties;

    }

    /**
     * Creates the configured Qdrant collection (1536-dim vectors, cosine
     * distance) if it does not already exist. A 409 response (collection
     * already exists) is treated as a no-op rather than an error.
     *
     * @throws RuntimeException if collection creation fails for any reason
     *                           other than the collection already existing
     */
    @Override
public void createCollection() {

    try {

        String body = """
                {
                  "vectors": {
                    "size": 1536,
                    "distance": "Cosine"
                  }
                }
                """;


        webClient.put()
                .uri("/collections/" + properties.getCollection())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        System.out.println("Qdrant collection created");

        } catch (Exception e) {
            if (e.getMessage().contains("409")) {
                System.out.println("Qdrant collection already exists");
            } else {
                throw e;
            }
        }

    }

    /**
     * Upserts a single ticket into Qdrant as a point, with its embedding
     * vector and the original ticket text as payload.
     *
     * @param ticketId   a unique identifier for the point (typically a UUID)
     * @param ticketText the original ticket message, stored as payload so it
     *                   can be shown alongside future similarity search results
     * @param vector     the embedding vector for {@code ticketText}
     */
    public void saveTicket(
            String ticketId,
            String ticketText,
            List<Float> vector
    ) {

        String body = """
                {
                "points": [
                    {
                    "id": "%s",
                    "vector": %s,
                    "payload": {
                        "ticket": "%s"
                    }
                    }
                ]
                }
                """.formatted(
                    ticketId,
                    vector.toString(),
                    ticketText.replace("\"", "\\\"")
                );


        webClient.put()
                .uri("/collections/" + properties.getCollection() + "/points")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        System.out.println("Ticket stored in Qdrant");

    }

    /**
     * Searches the configured Qdrant collection for the tickets most similar
     * to the given embedding vector.
     *
     * @param vector the embedding vector to search against
     * @return the raw JSON response from Qdrant's search endpoint (top 3
     *         matches, including payload)
     */
    public String searchSimilarTickets(List<Float> vector) {

    String body = """
            {
              "vector": %s,
              "limit": 3,
              "with_payload": true
            }
            """.formatted(vector.toString());


    return webClient.post()
            .uri("/collections/" 
                    + properties.getCollection()
                    + "/points/search")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .block();

}
}