package com.example.ticket_router.repository;

import com.example.ticket_router.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

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