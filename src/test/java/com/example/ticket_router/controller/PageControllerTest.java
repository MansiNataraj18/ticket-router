package com.example.ticket_router.controller;

import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PageController}, the ticket submission home page.
 * Called directly rather than through Spring MVC/security, so authentication
 * is supplied as a plain {@link UsernamePasswordAuthenticationToken}.
 */
class PageControllerTest {

    private final PageController pageController = new PageController();

    @Test
    void index_redirectsToLogin_whenNotAuthenticated() {
        String view = pageController.index(null, new ConcurrentModel());

        assertEquals("redirect:/login", view);
    }

    @Test
    void index_returnsIndexView_forAPlainCustomer() {
        Model model = new ConcurrentModel();

        String view = pageController.index(authenticationWith("alice", "ROLE_CUSTOMER", "SUBMIT_TICKET"), model);

        assertEquals("index", view);
        assertEquals("alice", model.asMap().get("userName"));
        assertFalse((Boolean) model.asMap().get("isAdmin"));
        assertFalse((Boolean) model.asMap().get("isDepartmentStaff"));
    }

    @Test
    void index_setsIsAdminTrue_whenCallerHasViewAllTicketsAuthority() {
        Model model = new ConcurrentModel();

        pageController.index(authenticationWith("admin", "ROLE_ADMIN", "VIEW_ALL_TICKETS"), model);

        assertTrue((Boolean) model.asMap().get("isAdmin"));
        assertFalse((Boolean) model.asMap().get("isDepartmentStaff"));
    }

    @Test
    void index_setsIsDepartmentStaffTrue_whenCallerHasViewDepartmentTicketsAuthority() {
        Model model = new ConcurrentModel();

        pageController.index(authenticationWith("staff1", "ROLE_ENGINEERING_STAFF", "VIEW_DEPARTMENT_TICKETS"), model);

        assertFalse((Boolean) model.asMap().get("isAdmin"));
        assertTrue((Boolean) model.asMap().get("isDepartmentStaff"));
    }

    private static UsernamePasswordAuthenticationToken authenticationWith(String username, String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<>();
        for (String authority : authorities) {
            granted.add(new SimpleGrantedAuthority(authority));
        }
        return new UsernamePasswordAuthenticationToken(username, "n/a", granted);
    }
}
