package com.example.ticket_router;

import com.example.ticket_router.service.TicketRoutingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    private final TicketRoutingService ticketRoutingService;

    public TestRunner(TicketRoutingService ticketRoutingService) {
        this.ticketRoutingService = ticketRoutingService;
    }

    @Override
    public void run(String... args) throws Exception {

        var result = ticketRoutingService.route(
                "can you add a a handicap accessibility feature to the product?"
        );

        System.out.println("Routing result:");
        System.out.println(result);
    }
}