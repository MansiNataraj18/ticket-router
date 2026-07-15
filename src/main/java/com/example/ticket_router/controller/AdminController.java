package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.repository.TicketRepository;

import jakarta.servlet.http.HttpSession;

//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class AdminController {

    //private static final String LOGGER = LoggerFactory.getLogger(AdminController.class).getName();

    private final TicketRepository ticketRepository;


    public AdminController(
            TicketRepository ticketRepository
    ) {

        this.ticketRepository = ticketRepository;

    }



    @GetMapping("/admin")
    public String adminDashboard(
            HttpSession session,
            Model model
    ) {
        //LOGGER.INFO("Accessing admin dashboard");

        String role =
                (String) session.getAttribute("role");


        if (!"ADMIN".equals(role)) {

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