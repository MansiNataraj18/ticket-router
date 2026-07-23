package com.example.ticket_router.repository;

import com.example.ticket_router.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserType} entities, which define the
 * roles/departments users can belong to and their granted permissions.
 */
public interface UserTypeRepository extends JpaRepository<UserType, Long> {

    /**
     * Finds a user type by its exact name.
     *
     * @param name the user type name to look up (e.g. {@code ENGINEERING_STAFF})
     * @return the matching user type, if one exists
     */
    Optional<UserType> findByName(String name);

    /**
     * @return every user type that represents department staff (i.e. has a
     *         non-null {@code department_team_name}), for populating the
     *         admin "create user" dropdown.
     */
    List<UserType> findByDepartmentTeamNameIsNotNull();
}
