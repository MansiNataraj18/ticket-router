package com.example.ticket_router.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.repository.UserProfileRepository;

@Controller
public class LoginController {

    private final UserProfileRepository userProfileRepository;

public LoginController(
        UserProfileRepository userProfileRepository
) {
    this.userProfileRepository = userProfileRepository;
}
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        if (session.getAttribute("username") != null) {

            String role = (String) session.getAttribute("role");

            if ("ADMIN".equals(role)) {
                return "redirect:/admin";
            }

            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            HttpSession session
    ) {

        UserProfile userProfile =
        userProfileRepository
                .findByName(username)
                .orElseGet(() ->
                        userProfileRepository.save(
                                new UserProfile(username)
                        )
                );


session.setAttribute(
        "userProfile",
        userProfile
);

session.setAttribute(
        "username",
        username
);

        if ("admin".equalsIgnoreCase(username)) {
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin";
        }

        session.setAttribute("role", "USER");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}