package com.example.ticket_router.repository;

import com.example.ticket_router.dto.Priority;
import com.example.ticket_router.entity.Ticket;
import com.example.ticket_router.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * @param user the user to look up tickets for
     * @return the user's tickets ordered by creation date, descending
     */
    List<Ticket> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds every ticket with the given priority, for the admin dashboard's
     * priority filter.
     *
     * @param priority the priority to filter by
     * @return all tickets at that priority level, in no particular order
     *         (the caller is expected to sort as needed)
     */
    List<Ticket> findByPriority(Priority priority);

    /**
     * Finds every ticket assigned to the given department/team, for a
     * department staff member's own dashboard.
     *
     * @param assignedTeam the exact team name (e.g. "Engineering Department")
     * @return that department's tickets, most recently created first
     */
    List<Ticket> findByAssignedTeamOrderByCreatedAtDesc(String assignedTeam);

    /**
     * Finds every ticket assigned to the given department/team at a given
     * priority, for the department dashboard's priority filter.
     *
     * @param assignedTeam the exact team name (e.g. "Engineering Department")
     * @param priority     the priority to filter by
     * @return that department's tickets at that priority level, most recently created first
     */
    List<Ticket> findByAssignedTeamAndPriorityOrderByCreatedAtDesc(String assignedTeam, Priority priority);

}