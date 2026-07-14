package com.example.ticket_router;

import com.example.ticket_router.client.QdrantClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class QdrantTestRunner implements CommandLineRunner {


    private final QdrantClient qdrantClient;


    public QdrantTestRunner(QdrantClient qdrantClient){
        this.qdrantClient = qdrantClient;
    }


    @Override
    public void run(String... args){

        qdrantClient.createCollection();

        System.out.println("Qdrant collection initialized");

    }

}