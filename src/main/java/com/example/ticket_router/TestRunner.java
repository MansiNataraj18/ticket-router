package com.example.ticket_router;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.EmbeddingService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class TestRunner implements CommandLineRunner {


    private final TicketRoutingService ticketRoutingService;
    private final EmbeddingService embeddingService;


    public TestRunner(
            TicketRoutingService ticketRoutingService,
            EmbeddingService embeddingService
    ) {

        this.ticketRoutingService = ticketRoutingService;
        this.embeddingService = embeddingService;

    }


    @Override
    public void run(String... args) throws Exception {


        var vector = embeddingService.generate(
                "My account is locked and I cannot login"
        );


        System.out.println("Embedding size:");
        System.out.println(vector.size());


        var result = ticketRoutingService.route(
                "can you add a handicap accessibility feature to the product?"
        );


        System.out.println("Routing result:");
        System.out.println(result);

    }

}