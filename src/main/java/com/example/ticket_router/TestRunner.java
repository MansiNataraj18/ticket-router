package com.example.ticket_router;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.EmbeddingService;
import com.example.ticket_router.service.QdrantService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class TestRunner implements CommandLineRunner {

    private final TicketRoutingService ticketRoutingService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;


    public TestRunner(
            TicketRoutingService ticketRoutingService,
            EmbeddingService embeddingService,
            QdrantService qdrantService
    ) {

        this.ticketRoutingService = ticketRoutingService;
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;

    }


    @Override
    public void run(String... args) throws Exception {


        // New incoming ticket
        String newTicket =
                "I forgot my password and cannot access my account";


        // Generate embedding for incoming ticket
        var newVector =
                embeddingService.generate(newTicket);


        System.out.println("Embedding size:");
        System.out.println(newVector.size());


        // Search similar historical tickets from Qdrant
        String similar =
                qdrantService.findSimilarTickets(newVector);


        System.out.println("Similar tickets:");
        System.out.println(similar);


        // Run AI routing with RAG context
        var result =
                ticketRoutingService.route(newTicket);


        System.out.println("Routing result:");
        System.out.println(result);

    }
}