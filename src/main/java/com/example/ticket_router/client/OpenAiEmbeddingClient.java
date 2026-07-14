package com.example.ticket_router.client;


import com.example.ticket_router.dto.EmbeddingResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


@Component
public class OpenAiEmbeddingClient {


    private final WebClient webClient;


    public OpenAiEmbeddingClient(WebClient openAiWebClient) {
        this.webClient = openAiWebClient;
    }


    public List<Float> createEmbedding(String text) {


        Map<String, Object> request = Map.of(
                "model", "text-embedding-3-small",
                "input", text
        );


        EmbeddingResponse response = webClient.post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();


        return response.getData()
                .get(0)
                .getEmbedding()
                .stream()
                .map(Double::floatValue)
                .toList();

    }

}