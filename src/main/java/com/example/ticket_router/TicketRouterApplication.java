package com.example.ticket_router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.ticket_router.config.OpenAiProperties;
import com.example.ticket_router.config.QdrantProperties;


@SpringBootApplication
@EnableConfigurationProperties({
        OpenAiProperties.class,
        QdrantProperties.class
})
public class TicketRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketRouterApplication.class, args);
    }

}