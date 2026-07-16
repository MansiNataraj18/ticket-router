package com.example.ticket_router.controller;

import com.example.ticket_router.entity.Role;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Handles self-service account registration.
 * <p>
 * New accounts are always created with {@link Role#USER}; there is
 * intentionally no way for a caller to self-assign {@code ADMIN} or
 * {@code AGENT} through this endpoint.
 */
@Controller
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    /**
     * @param userRepository   used to check for existing usernames and persist new users
     * @param passwordEncoder  used to hash the submitted password before storage
     */
    public SignupController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * @return the {@code signup} view template
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }


    /**
     * Validates and creates a new {@code ROLE_USER} account.
     *
     * @param username the desired, unique username
     * @param password the plaintext password, hashed with {@link PasswordEncoder} before storage
     * @param fullName the user's display name
     * @param model    the Spring MVC model, populated with an {@code error} attribute on failure
     * @return a redirect to {@code /login?signup} on success, otherwise back to the
     *         {@code signup} view with an error message
     */
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

            log.warn("Signup rejected: missing required fields");
            model.addAttribute("error", "All fields are required.");
            return "signup";
        }

        if (userRepository.findByUsername(username.trim()).isPresent()) {

            log.warn("Signup rejected: username '{}' already exists", username);
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

            log.info("New user registered: '{}'", username);

        } catch (DataIntegrityViolationException e) {

            log.warn("Signup failed due to a database constraint for username '{}'", username, e);
            model.addAttribute("error", "That username is already taken.");
            return "signup";
        }

        return "redirect:/login?signup";
    }

}