package com.example.ticket_router.controller;

import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.repository.UserTypeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only screen for creating role-specific (staff/department, or even
 * another admin) accounts.
 * <p>
 * This is separate from {@link SignupController}, which remains the public,
 * self-service path for ticket-submitting customers and always creates
 * {@code CUSTOMER} accounts. Only an admin, here, can create an account with
 * any other {@link UserType}.
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param userRepository     used to list existing users and persist new ones
     * @param userTypeRepository used to populate the user-type dropdown
     * @param passwordEncoder    used to hash the submitted password before storage
     */
    public AdminUserController(
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Renders the "create user" screen along with the current user list,
     * optionally narrowed down to a single user type.
     *
     * @param authentication the current request's authentication
     * @param userTypeName   optional user type name to filter the list by (e.g. {@code ENGINEERING_STAFF});
     *                       when absent or unrecognized, every user is shown
     * @param model          the Spring MVC model to populate for the view
     * @return the {@code admin-users} view
     */
    @GetMapping
    public String manageUsers(
            Authentication authentication,
            @RequestParam(name = "type", required = false) String userTypeName,
            Model model
    ) {
        log.info(
                "Admin '{}' opened the user management screen (type filter={})",
                authentication.getName(),
                userTypeName
        );
        populateCommonAttributes(authentication, model, userTypeName);
        return "admin-users";
    }

    /**
     * Creates a new user with an admin-selected user type.
     *
     * @param username     the new account's unique username
     * @param password     the plaintext password, hashed before storage
     * @param fullName     the new account's display name
     * @param userTypeName the name of the {@link UserType} to assign (e.g. {@code ENGINEERING_STAFF})
     * @param authentication the current request's authentication
     * @param model        the Spring MVC model, populated with an {@code error} attribute on failure
     * @return a redirect back to {@code /admin/users} on success, otherwise the
     *         same view with an error message
     */
    @PostMapping
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String userTypeName,
            Authentication authentication,
            Model model
    ) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || fullName == null || fullName.isBlank()
                || userTypeName == null || userTypeName.isBlank()) {
            log.warn("Admin '{}' submitted an incomplete create-user form", authentication.getName());
            model.addAttribute("error", "All fields are required.");
            populateCommonAttributes(authentication, model, null);
            return "admin-users";
        }

        if (userRepository.findByUsername(username.trim()).isPresent()) {
            log.warn("Admin '{}' attempted to create a duplicate username '{}'", authentication.getName(), username);
            model.addAttribute("error", "That username is already taken.");
            populateCommonAttributes(authentication, model, null);
            return "admin-users";
        }

        try {
            UserType userType = userTypeRepository.findByName(userTypeName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown user type: " + userTypeName));

            User user = new User(
                    username.trim(),
                    passwordEncoder.encode(password),
                    fullName.trim(),
                    userType
            );

            userRepository.save(user);
            log.info(
                    "Admin '{}' created new '{}' user '{}'",
                    authentication.getName(),
                    userTypeName,
                    username
            );
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            log.warn("Failed to create user '{}': {}", username, e.getMessage());
            model.addAttribute("error", "Could not create that user: " + e.getMessage());
            populateCommonAttributes(authentication, model, null);
            return "admin-users";
        }

        return "redirect:/admin/users?created";
    }

    /**
     * Populates the model attributes shared by every render of the
     * {@code admin-users} view, including the (optionally type-filtered)
     * user list.
     *
     * @param authentication the current request's authentication
     * @param model          the Spring MVC model to populate
     * @param selectedTypeName the {@code UserType} name to filter the user list
     *                          by, or {@code null}/blank/unrecognized to show every user
     */
    private void populateCommonAttributes(Authentication authentication, Model model, String selectedTypeName) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("VIEW_ALL_TICKETS"));

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isDepartmentStaff", false);
        model.addAttribute("userTypes", userTypeRepository.findAll());

        boolean hasTypeFilter = selectedTypeName != null && !selectedTypeName.isBlank();
        model.addAttribute(
                "users",
                hasTypeFilter
                        ? userRepository.findByUserType_Name(selectedTypeName)
                        : userRepository.findAll()
        );
        model.addAttribute("selectedUserType", hasTypeFilter ? selectedTypeName : null);
    }
}
