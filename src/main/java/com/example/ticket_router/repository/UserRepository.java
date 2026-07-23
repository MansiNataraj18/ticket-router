package com.example.ticket_router.repository;

import com.example.ticket_router.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities, used for
 * authentication lookups and the admin "Manage Users" screen.
 */
public interface UserRepository
        extends JpaRepository<User, Long> {

    /**
     * Finds a user by their login username, for authentication.
     *
     * @param username the username to look up
     * @return the matching user, if one exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds every user of a given {@code UserType}, for the "Manage Users"
     * screen's type filter.
     *
     * @param userTypeName the {@code UserType} name to filter by (e.g. {@code ENGINEERING_STAFF})
     * @return that type's users
     */
    List<User> findByUserType_Name(String userTypeName);
}