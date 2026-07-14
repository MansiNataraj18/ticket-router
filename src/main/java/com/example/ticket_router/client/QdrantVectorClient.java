package com.example.ticket_router.client;


import com.example.ticket_router.config.QdrantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


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

}