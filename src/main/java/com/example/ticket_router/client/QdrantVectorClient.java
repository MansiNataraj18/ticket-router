package com.example.ticket_router.client;


import com.example.ticket_router.config.QdrantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
//import java.util.Map;

@Component
public class QdrantVectorClient implements QdrantClient {


    private final WebClient webClient;
    private final QdrantProperties properties;


    public QdrantVectorClient(
            WebClient qdrantWebClient,
            QdrantProperties properties
    ) {

        this.webClient = qdrantWebClient;
        this.properties = properties;

    }

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