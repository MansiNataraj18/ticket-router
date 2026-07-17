package com.example.ticket_router.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    @Test
    void healthCheck_returnsOk() {
        assertEquals("OK", new HealthController().healthCheck());
    }
}
