package com.example.ticket_router.entity;

import jakarta.persistence.*;

/**
 * A single, named permission (e.g. {@code VIEW_ALL_TICKETS}) that can be
 * granted to one or more {@link UserType}s via the {@code user_type_permissions}
 * mapping table. Spring Security authorizes requests against these names
 * directly (see {@link com.example.ticket_router.service.CustomUserDetailsService}).
 */
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_name", nullable = false, unique = true, length = 100)
    private String permissionName;

    @Column(name = "permission_description", length = 255)
    private String permissionDescription;

    public Permission() {
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getPermissionDescription() {
        return permissionDescription;
    }
}
