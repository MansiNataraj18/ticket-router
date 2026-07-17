package com.example.ticket_router.controller;

import com.example.ticket_router.entity.User;
import com.example.ticket_router.entity.UserType;
import com.example.ticket_router.repository.UserRepository;
import com.example.ticket_router.repository.UserTypeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminUserController}: creating staff accounts with a
 * given {@link UserType}, and filtering the "Existing Users" list by type.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTypeRepository userTypeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        adminUserController = new AdminUserController(userRepository, userTypeRepository, passwordEncoder);
    }

    @Test
    void manageUsers_listsEveryUser_whenNoTypeFilterGiven() {
        List<User> allUsers = List.of(new User("bob", "hash", "Bob", null));
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userTypeRepository.findAll()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = adminUserController.manageUsers(authenticationWith("admin", "VIEW_ALL_TICKETS"), null, model);

        assertEquals("admin-users", view);
        assertEquals(allUsers, model.asMap().get("users"));
        assertNull(model.asMap().get("selectedUserType"));
    }

    @Test
    void manageUsers_filtersUsersByType_whenTypeParamGiven() {
        List<User> engineeringUsers = List.of(new User("staff1", "hash", "Staff One", null));
        when(userRepository.findByUserType_Name("ENGINEERING_STAFF")).thenReturn(engineeringUsers);
        when(userTypeRepository.findAll()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = adminUserController.manageUsers(
                authenticationWith("admin", "VIEW_ALL_TICKETS"), "ENGINEERING_STAFF", model);

        assertEquals("admin-users", view);
        assertEquals(engineeringUsers, model.asMap().get("users"));
        assertEquals("ENGINEERING_STAFF", model.asMap().get("selectedUserType"));
    }

    @Test
    void createUser_savesNewUser_whenInputIsValid() {
        UserType supportStaff = new UserType();
        ReflectionTestUtils.setField(supportStaff, "name", "SUPPORT_STAFF");

        when(userRepository.findByUsername("newstaff")).thenReturn(Optional.empty());
        when(userTypeRepository.findByName("SUPPORT_STAFF")).thenReturn(Optional.of(supportStaff));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");

        String view = adminUserController.createUser(
                "newstaff", "secret123", "New Staff", "SUPPORT_STAFF",
                authenticationWith("admin", "MANAGE_USERS"), new ConcurrentModel());

        assertEquals("redirect:/admin/users?created", view);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("newstaff", captor.getValue().getUsername());
        assertEquals("hashed-secret", captor.getValue().getPassword());
        assertEquals(supportStaff, captor.getValue().getUserType());
    }

    @Test
    void createUser_showsError_whenFieldsAreBlank() {
        String view = adminUserController.createUser(
                "", "secret123", "New Staff", "SUPPORT_STAFF",
                authenticationWith("admin", "MANAGE_USERS"), new ConcurrentModel());

        assertEquals("admin-users", view);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_showsError_whenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(new User("bob", "hash", "Bob", null)));
        when(userTypeRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        String view = adminUserController.createUser(
                "bob", "secret123", "Bob", "SUPPORT_STAFF",
                authenticationWith("admin", "MANAGE_USERS"), new ConcurrentModel());

        assertEquals("admin-users", view);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_showsError_whenUserTypeIsUnknown() {
        when(userRepository.findByUsername("newstaff")).thenReturn(Optional.empty());
        when(userTypeRepository.findByName("NOT_A_TYPE")).thenReturn(Optional.empty());
        when(userTypeRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        String view = adminUserController.createUser(
                "newstaff", "secret123", "New Staff", "NOT_A_TYPE",
                authenticationWith("admin", "MANAGE_USERS"), new ConcurrentModel());

        assertEquals("admin-users", view);
        verify(userRepository, never()).save(any());
    }

    private static UsernamePasswordAuthenticationToken authenticationWith(String username, String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<>();
        for (String authority : authorities) {
            granted.add(new SimpleGrantedAuthority(authority));
        }
        return new UsernamePasswordAuthenticationToken(username, "n/a", granted);
    }
}
