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

        String ticket =
                "Customer cannot login to their account";


        var vector = embeddingService.generate(ticket);


        System.out.println("Embedding size:");
        System.out.println(vector.size());

        qdrantService.storeTicket(
                ticket,
                vector
        );

        var result = ticketRoutingService.route(
                "can you add a handicap accessibility feature to the product?"
        );

        System.out.println("Routing result:");
        System.out.println(result);

    }

}

