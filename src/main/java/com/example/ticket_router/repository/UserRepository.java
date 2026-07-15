package com.example.ticket_router.repository;

import com.example.ticket_router.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository 
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}