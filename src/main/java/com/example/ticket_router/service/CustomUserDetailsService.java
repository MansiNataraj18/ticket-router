package com.example.ticket_router.service;

import com.example.ticket_router.entity.Permission;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.repository.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * Bridges the application's {@link User} entity to Spring Security's
 * authentication model.
 * <p>
 * Looked up automatically by Spring Security's {@code DaoAuthenticationProvider}
 * (the sole {@link UserDetailsService} bean) whenever a login attempt or
 * an authenticated request needs to load the current user's credentials
 * and authorities.
 * <p>
 * Two kinds of authority are granted: a single {@code ROLE_<user type name>}
 * authority (used to identify which specific type/department a user belongs
 * to), and one authority per {@link Permission} their {@link UserType} has
 * been granted in the {@code user_type_permissions} mapping table (used for
 * fine-grained {@code hasAuthority(...)} checks in {@link
 * com.example.ticket_router.config.SecurityConfig}).
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
     *         password, a {@code ROLE_<user type name>} authority, and one
     *         authority per permission granted to their user type
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

        UserType userType = user.getUserType();

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + userType.getName()));

        for (Permission permission : userType.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
        }


        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}
