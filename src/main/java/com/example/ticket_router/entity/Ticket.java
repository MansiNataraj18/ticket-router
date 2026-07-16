package com.example.ticket_router.entity;

import com.example.ticket_router.dto.Priority;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5000, nullable = false)
    private String message;

    private String category;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private String assignedTeam;

    @Column(length = 1000)
    private String reasoning;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Ticket() {
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public String getReasoning() {
        return reasoning;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public void setUser(User user) {
        this.user = user;
    }
}