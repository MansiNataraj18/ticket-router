package com.example.ticket_router.service;

import com.example.ticket_router.entity.User;
import com.example.ticket_router.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;


/**
 * Bridges the application's {@link User} entity to Spring Security's
 * authentication model.
 * <p>
 * Looked up automatically by Spring Security's {@code DaoAuthenticationProvider}
 * (the sole {@link UserDetailsService} bean) whenever a login attempt or
 * an authenticated request needs to load the current user's credentials
 * and authorities.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;


    /**
     * @param userRepository used to look up the persisted {@link User} by username
     */
    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }


    /**
     * Loads a user's authentication details by username.
     *
     * @param username the username to look up
     * @return a Spring Security {@code UserDetails} with the user's hashed
     *         password and a single {@code ROLE_<role>} authority derived from
     *         {@link User#getRole()}
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {


        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"
                                )
                        );


        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}