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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SignupController}, the public self-service signup
 * path that always creates a {@code CUSTOMER} account.
 */
@ExtendWith(MockitoExtension.class)
class SignupControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTypeRepository userTypeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SignupController signupController;

    @BeforeEach
    void setUp() {
        signupController = new SignupController(userRepository, userTypeRepository, passwordEncoder);
    }

    @Test
    void signupPage_returnsSignupView() {
        assertEquals("signup", signupController.signupPage());
    }

    @Test
    void signup_createsCustomerAccount_whenInputIsValid() {
        UserType customerType = new UserType();
        ReflectionTestUtils.setField(customerType, "name", "CUSTOMER");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userTypeRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerType));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");

        String view = signupController.signup("newuser", "secret123", "New User", new ConcurrentModel());

        assertEquals("redirect:/login?signup", view);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals("hashed-secret", captor.getValue().getPassword());
        assertEquals(customerType, captor.getValue().getUserType());
    }

    @Test
    void signup_showsError_whenFieldsAreBlank() {
        Model model = new ConcurrentModel();
        String view = signupController.signup("", "secret123", "New User", model);

        assertEquals("signup", view);
        assertNotNull(model.asMap().get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_showsError_whenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(new User("bob", "hash", "Bob", null)));

        Model model = new ConcurrentModel();
        String view = signupController.signup("bob", "secret123", "Bob", model);

        assertEquals("signup", view);
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_showsError_whenSaveFailsDueToDataIntegrityViolation() {
        UserType customerType = new UserType();
        ReflectionTestUtils.setField(customerType, "name", "CUSTOMER");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userTypeRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerType));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        doThrow(new DataIntegrityViolationException("duplicate key"))
                .when(userRepository).save(any());

        Model model = new ConcurrentModel();
        String view = signupController.signup("newuser", "secret123", "New User", model);

        assertEquals("signup", view);
        assertNotNull(model.asMap().get("error"));
    }
}
