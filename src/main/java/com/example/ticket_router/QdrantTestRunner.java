package com.example.ticket_router;

import com.example.ticket_router.client.QdrantClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * Startup runner that verifies Qdrant connectivity by ensuring the configured
 * collection exists. Runs once automatically on application boot, alongside
 * any other {@link CommandLineRunner} beans.
 */
@Component
public class QdrantTestRunner implements CommandLineRunner {


    private final QdrantClient qdrantClient;


    public QdrantTestRunner(QdrantClient qdrantClient){
        this.qdrantClient = qdrantClient;
    }


    /**
     * Triggers creation of the Qdrant collection (a no-op if it already
     * exists) so the application fails fast if Qdrant is unreachable.
     *
     * @param args command-line arguments passed to the application (unused)
     */
    @Override
    public void run(String... args){

        qdrantClient.createCollection();

        System.out.println("Qdrant collection initialized");

    }

}