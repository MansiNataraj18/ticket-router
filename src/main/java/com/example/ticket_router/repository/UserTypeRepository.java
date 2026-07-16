package com.example.ticket_router.repository;

import com.example.ticket_router.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {

    Optional<UserType> findByName(String name);

    /**
     * @return every user type that represents department staff (i.e. has a
     *         non-null {@code department_team_name}), for populating the
     *         admin "create user" dropdown.
     */
    List<UserType> findByDepartmentTeamNameIsNotNull();
}
