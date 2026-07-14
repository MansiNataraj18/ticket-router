package com.example.ticket_router.service;

import com.example.ticket_router.client.OpenAiEmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class EmbeddingService {

    private final OpenAiEmbeddingClient embeddingClient;

    public EmbeddingService(OpenAiEmbeddingClient embeddingClient){
        this.embeddingClient = embeddingClient;
    }

    public List<Float> generate(String text){
        return embeddingClient.createEmbedding(text);
    }

}