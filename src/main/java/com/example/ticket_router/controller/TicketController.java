package com.example.ticket_router.controller;

import com.example.ticket_router.service.TicketRoutingService;
import com.example.ticket_router.service.TicketService;
import com.example.ticket_router.dto.TicketRequest;
import com.example.ticket_router.dto.TicketRoutingResult;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.exception.UserNotFoundException;
import com.example.ticket_router.repository.UserRepository;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


/**
 * REST endpoint that classifies (routes) a support ticket using the
 * Retrieval-Augmented Generation pipeline in {@link TicketRoutingService},
 * and persists the ticket against the caller's account if they're logged in.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketRoutingService service;

    private final TicketService ticketService;

    private final UserRepository userRepository;



    /**
     * @param service        performs the embed &rarr; search &rarr; LLM routing pipeline
     * @param ticketService  persists routed tickets against a {@link
     *                       com.example.ticket_router.entity.User}
     * @param userRepository looks up the authenticated user's account
     */
    public TicketController(
            TicketRoutingService service,
            TicketService ticketService,
            UserRepository userRepository
    ) {

        this.service = service;
        this.ticketService = ticketService;
        this.userRepository = userRepository;

    }



    /**
     * Classifies the submitted ticket message and, if the caller is
     * authenticated, saves the ticket to their history.
     *
     * @param request        the incoming ticket, containing the raw message text
     * @param authentication the current request's authentication, or {@code null}
     *                        if the caller is anonymous
     * @return the AI-generated category, priority, assigned team, and reasoning
     * @throws com.example.ticket_router.exception.InvalidTicketException if the message
     *         fails validation (blank, too short, or too long)
     * @throws com.example.ticket_router.exception.RoutingException if the embedding,
     *         Qdrant search, or LLM call fails
     * @throws UserNotFoundException if the caller is authenticated but their
     *         account can no longer be found
     */
    @PostMapping("/route")
    public TicketRoutingResult route(
            @Valid @RequestBody TicketRequest request,
            Authentication authentication
    )  {

        String username = authentication != null ? authentication.getName() : "anonymous";

        log.info("User '{}' submitted a ticket for routing", username);

        TicketRoutingResult result =
                service.route(
                        request.message()
                );

        log.info(
                "Ticket routed for user '{}': category={}, priority={}, team={}",
                username,
                result.category(),
                result.priority(),
                result.assignedTeam()
        );


        if (authentication != null && authentication.isAuthenticated()) {

            User user =
                    userRepository.findByUsername(authentication.getName())
                            .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

            ticketService.saveTicket(
                    request.message(),
                    user,
                    result
            );

        }


        return result;

    }

}
