package com.example.ticket_router.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Unauthenticated access to '/' - redirecting to login");
            return "redirect:/login";
        }

        log.info("User '{}' opened the ticket submission page", authentication.getName());

        model.addAttribute("userName", authentication.getName());

        return "index";
    }
}