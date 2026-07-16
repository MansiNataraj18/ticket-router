package com.example.ticket_router.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness endpoint for load balancers, uptime checks, and manual
 * "is the app up?" checks.
 */
@RestController
public class HealthController {

    /**
     * @return the literal string {@code "OK"} if the application is running
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
