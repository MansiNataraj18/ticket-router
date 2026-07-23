package com.example.ticket_router.entity;

import jakarta.persistence.*;

/**
 * A registered account (customer, department staff, or admin), persisted in
 * the {@code users} table. Each user belongs to exactly one {@link UserType},
 * which determines their granted {@link Permission}s, and may own zero or
 * more {@link Ticket}s.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserType userType;

    @Column(nullable = false)
    private boolean enabled = true;

    public User() {
    }

    public User(
            String username,
            String password,
            String fullName,
            UserType userType
    ) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.userType = userType;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public UserType getUserType() {
        return userType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
