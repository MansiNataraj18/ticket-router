package com.example.ticket_router;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.EmbeddingService;
import com.example.ticket_router.service.QdrantService;
//import com.example.ticket_router.service.QdrantService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//import java.util.List;



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


    // Add historical ticket knowledge to Qdrant
    String[] historicalTickets = {
            "Customer cannot reset password",
            "Customer cannot login to their account",
            "Payment failed during checkout",
            "Application crashes when opening dashboard",
            "User wants accessibility features in the product"
    };


    for (String historicalTicket : historicalTickets) {

        var historicalVector =
                embeddingService.generate(historicalTicket);


        qdrantService.storeTicket(
                historicalTicket,
                historicalVector
        );
    }


    // New incoming ticket
    String newTicket =
            "I forgot my password and cannot access my account";


    var newVector =
            embeddingService.generate(newTicket);


    System.out.println("Embedding size:");
    System.out.println(newVector.size());


    // Search similar tickets
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