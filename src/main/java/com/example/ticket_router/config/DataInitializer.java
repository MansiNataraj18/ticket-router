package com.example.ticket_router.config;

import com.example.ticket_router.service.EmbeddingService;
import com.example.ticket_router.service.QdrantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;

    public DataInitializer(
            EmbeddingService embeddingService,
            QdrantService qdrantService
    ) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
    }

    @Override
    public void run(String... args) {

        List<String> tickets = List.of(
                "Customer cannot reset password",
                "Customer cannot login to their account",
                "Payment failed during checkout",
                "Application crashes when opening dashboard",
                "User wants accessibility features in the product"
        );

        for (String ticket : tickets) {

            List<Float> embedding =
                    embeddingService.generate(ticket);

            qdrantService.storeTicket(ticket, embedding);

            System.out.println(
                    "Seeded ticket: " + ticket
            );
        }
    }
}