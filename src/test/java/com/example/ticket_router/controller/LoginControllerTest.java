package com.example.ticket_router.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginControllerTest {

    @Test
    void loginPage_returnsLoginView() {
        assertEquals("login", new LoginController().loginPage());
    }
}
