package com.example.ticket_router.repository;

import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserProfileOrderByCreatedAtDesc(UserProfile userProfile);

}