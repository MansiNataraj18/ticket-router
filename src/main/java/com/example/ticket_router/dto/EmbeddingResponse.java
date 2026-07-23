package com.example.ticket_router.dto;

import java.util.List;

/**
 * Maps the JSON body of the OpenAI embeddings API response, as deserialized
 * by {@link com.example.ticket_router.client.OpenAiEmbeddingClient}. Carries
 * the list of returned embedding vectors, one per input text.
 */
public class EmbeddingResponse {

    private List<EmbeddingData> data;

    public List<EmbeddingData> getData() {
        return data;
    }

    public void setData(List<EmbeddingData> data) {
        this.data = data;
    }

    /**
     * A single embedding result within an {@link EmbeddingResponse}, holding
     * the numeric vector produced for one input text.
     */
    public static class EmbeddingData {

        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
