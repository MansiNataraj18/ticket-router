package com.example.ticket_router.config;

import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.repository.UserTypeRepository;
import com.example.ticket_router.service.EmbeddingService;
import com.example.ticket_router.service.QdrantService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmbeddingService embeddingService;

    private final QdrantService qdrantService;

    private final UserRepository userRepository;

    private final UserTypeRepository userTypeRepository;

    private final PasswordEncoder passwordEncoder;


    public DataInitializer(
            EmbeddingService embeddingService,
            QdrantService qdrantService,
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) {


        createUsers();


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


            qdrantService.storeTicket(
                    ticket,
                    embedding
            );


            log.info("Seeded ticket: {}", ticket);
        }
    }


    private void createUsers() {


        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping default user seeding");
            return;
        }


        UserType admin = requireUserType("ADMIN");
        UserType customer = requireUserType("CUSTOMER");
        UserType supportStaff = requireUserType("SUPPORT_STAFF");


        userRepository.save(
                new User(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "System Admin",
                        admin
                )
        );


        userRepository.save(
                new User(
                        "agent",
                        passwordEncoder.encode("agent123"),
                        "Support Agent",
                        supportStaff
                )
        );


        userRepository.save(
                new User(
                        "user",
                        passwordEncoder.encode("user123"),
                        "Normal User",
                        customer
                )
        );


        log.info("Default users created");
    }


    private UserType requireUserType(String name) {

        return userTypeRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Expected user_type '" + name + "' to exist - check migration V4"
                ));
    }
}
