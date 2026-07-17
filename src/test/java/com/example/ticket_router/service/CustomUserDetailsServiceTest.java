package com.example.ticket_router.service;

import com.example.ticket_router.entity.Permission;
import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomUserDetailsService}, verifying that a
 * {@link User}'s user type and permissions are translated into the
 * {@code ROLE_<type>} plus per-permission {@link GrantedAuthority} list that
 * {@code hasAuthority(...)} checks rely on elsewhere in the app.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_returnsRoleAndPermissionAuthorities() {

        Permission viewAll = new Permission();
        ReflectionTestUtils.setField(viewAll, "permissionName", "VIEW_ALL_TICKETS");

        Permission manageUsers = new Permission();
        ReflectionTestUtils.setField(manageUsers, "permissionName", "MANAGE_USERS");

        Set<Permission> permissions = new HashSet<>(Set.of(viewAll, manageUsers));

        UserType adminType = new UserType();
        ReflectionTestUtils.setField(adminType, "name", "ADMIN");
        ReflectionTestUtils.setField(adminType, "permissions", permissions);

        User user = new User("admin", "hashed-password", "System Admin", adminType);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("admin");

        List<String> authorityNames = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals("admin", details.getUsername());
        assertEquals("hashed-password", details.getPassword());
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("VIEW_ALL_TICKETS"));
        assertTrue(authorityNames.contains("MANAGE_USERS"));
        assertEquals(3, authorityNames.size());
    }

    @Test
    void loadUserByUsername_marksAccountDisabled_whenUserIsDisabled() {

        UserType customerType = new UserType();
        ReflectionTestUtils.setField(customerType, "name", "CUSTOMER");

        User user = new User("bob", "hashed-password", "Bob", customerType);
        user.setEnabled(false);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("bob");

        assertFalse(details.isEnabled());
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserDoesNotExist() {

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("ghost"));
    }
}
