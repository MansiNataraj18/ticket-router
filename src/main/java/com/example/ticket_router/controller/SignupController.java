package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Role;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class SignupController {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public SignupController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }


    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            Model model
    ) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || fullName == null || fullName.isBlank()) {

            model.addAttribute("error", "All fields are required.");
            return "signup";
        }

        if (userRepository.findByUsername(username.trim()).isPresent()) {

            model.addAttribute("error", "That username is already taken.");
            return "signup";
        }

        try {

            User user = new User(
                    username.trim(),
                    passwordEncoder.encode(password),
                    fullName.trim(),
                    Role.USER
            );

            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {

            model.addAttribute("error", "That username is already taken.");
            return "signup";
        }

        return "redirect:/login?signup";
    }

}