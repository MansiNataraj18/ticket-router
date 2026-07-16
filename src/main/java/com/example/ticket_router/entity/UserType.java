package com.example.ticket_router.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A type of user account (e.g. {@code ADMIN}, {@code CUSTOMER}, or one of
 * the five department staff types), replacing the old fixed {@code Role} enum.
 * <p>
 * Each {@link User} belongs to exactly one {@code UserType}, and each
 * {@code UserType} is granted zero or more {@link Permission}s via the
 * many-to-many {@code user_type_permissions} mapping table below. For
 * department staff types, {@link #departmentTeamName} holds the exact
 * {@code assignedTeam} string the AI router produces (see {@code
 * TicketRoutingPrompt}), so a staff member's own department tickets can be
 * found with a simple equality check; it is {@code null} for {@code ADMIN}
 * and {@code CUSTOMER}.
 */
@Entity
@Table(name = "user_type")
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "department_team_name", length = 100)
    private String departmentTeamName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_type_permissions",
            joinColumns = @JoinColumn(name = "user_type_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public UserType() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDepartmentTeamName() {
        return departmentTeamName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * @return {@code true} if this user type represents department staff
     *         (i.e. has a non-null {@link #departmentTeamName})
     */
    public boolean isDepartmentStaff() {
        return departmentTeamName != null;
    }
}
