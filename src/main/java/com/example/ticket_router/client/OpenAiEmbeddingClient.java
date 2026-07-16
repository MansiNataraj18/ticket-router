package com.example.ticket_router.client;


import com.example.ticket_router.dto.EmbeddingResponse;

import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


/**
 * Client responsible for generating text embeddings via the OpenAI Embeddings API.
 * <p>
 * Wraps a {@link WebClient} configured with the OpenAI base URL and API key
 * (see {@link com.example.ticket_router.config.OpenAiConfig}) and converts the
 * raw API response into a plain list of {@code Float} values that can be
 * stored in, and searched against, Qdrant.
 */
@Component
public class OpenAiEmbeddingClient {


    private final WebClient webClient;


    /**
     * @param openAiWebClient a {@link WebClient} pre-configured with the OpenAI
     *                         base URL and {@code Authorization} header
     */
    public OpenAiEmbeddingClient(WebClient openAiWebClient) {
        this.webClient = openAiWebClient;
    }


    /**
     * Requests an embedding vector for the given text from OpenAI's
     * {@code text-embedding-3-small} model.
     * <p>
     * Annotated with {@link Retry @Retry}, which wraps this method in a
     * Spring AOP proxy: on failure it is retried automatically according to
     * the {@code resilience4j.retry.instances.openai} settings in
     * {@code application.properties} (currently 3 attempts, 1s apart)
     * before the exception is allowed to propagate.
     *
     * @param text the text to embed
     * @return the embedding vector as a list of floats
     * @throws RuntimeException if the OpenAI API call still fails after all
     *                           retry attempts, or returns a response with
     *                           no embedding data
     */
    @Retry(name = "openai")
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