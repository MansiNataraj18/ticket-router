/*package com.example.ticket_router.controller;

//used to store session data
import jakarta.servlet.http.HttpSession;

//used for logging 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.repository.UserProfileRepository;


@Controller
public class LoginController {

    //creates a logger for this class 
    private static final Logger log =
            LoggerFactory.getLogger(LoginController.class);


    private final UserProfileRepository userProfileRepository;



    public LoginController(
            UserProfileRepository userProfileRepository
    ) {

        this.userProfileRepository = userProfileRepository;

    }



    @GetMapping("/login")
    public String loginPage(
            HttpSession session
    ) {


        String username =
                (String) session.getAttribute("username");


        if (username != null) {


            String role =
                    (String) session.getAttribute("role");


            log.info(
                    "Existing session found for user={}, role={}",
                    username,
                    role
            );


            if ("ADMIN".equals(role)) {

                log.info(
                        "Redirecting admin user to dashboard"
                );

                return "redirect:/admin";

            }


            log.info(
                    "Redirecting user {} to dashboard",
                    username
            );


            return "redirect:/";

        }


        log.info(
                "No active session. Showing login page"
        );


        return "login";

    }




    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            HttpSession session
    ) {


        log.info(
                "Login attempt for username={}",
                username
        );



        UserProfile userProfile =
                userProfileRepository
                        .findByName(username)
                        .orElseGet(() -> {


                            log.info(
                                    "Creating new user profile for username={}",
                                    username
                            );


                            return userProfileRepository.save(
                                    new UserProfile(username)
                            );

                        });



        session.setAttribute(
                "userProfile",
                userProfile
        );


        session.setAttribute(
                "username",
                username
        );



        if ("admin".equalsIgnoreCase(username)) {


            session.setAttribute(
                    "role",
                    "ADMIN"
            );


            log.info(
                    "Admin login successful for username={}",
                    username
            );


            return "redirect:/admin";

        }



        session.setAttribute(
                "role",
                "USER"
        );


        log.info(
                "User login successful for username={}",
                username
        );


        return "redirect:/";

    }





    @GetMapping("/logout")
    public String logout(
            HttpSession session
    ) {


        String username =
                (String) session.getAttribute("username");


        log.info(
                "Logout request for username={}",
                username
        );


        session.invalidate();


        log.info(
                "Session invalidated successfully"
        );


        return "redirect:/login";

    }
}*/

package com.example.ticket_router.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the login page.
 * <p>
 * Authentication itself (handling the {@code POST /login} submission) is
 * performed entirely by Spring Security's form-login filter, configured in
 * {@link com.example.ticket_router.config.SecurityConfig}; this controller
 * only renders the view.
 */
@Controller
public class LoginController {

    /**
     * @return the {@code login} view template
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}