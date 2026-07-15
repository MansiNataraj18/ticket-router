package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.repository.TicketRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class AdminController {

    private final TicketRepository ticketRepository;


    public AdminController(
            TicketRepository ticketRepository
    ) {

        this.ticketRepository = ticketRepository;

    }



    @GetMapping("/admin")
    public String adminDashboard(
            Authentication authentication,
            Model model
    ) {

        boolean isAdmin =
                authentication != null &&
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority -> authority.equals("ROLE_ADMIN"));


        if (!isAdmin) {

            return "redirect:/";

        }



        List<Ticket> tickets =
                ticketRepository
                        .findAll();



        model.addAttribute(
                "tickets",
                tickets
        );



        return "admin";

    }

}