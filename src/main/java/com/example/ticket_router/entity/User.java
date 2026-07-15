package com.example.ticket_router.entity;

import jakarta.persistence.*;

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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    @Column(nullable = false)
    private boolean enabled = true;


    public User() {
    }


    public User(
            String username,
            String password,
            String fullName,
            Role role
    ) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
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


    public Role getRole() {
        return role;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public void setRole(Role role) {
        this.role = role;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}