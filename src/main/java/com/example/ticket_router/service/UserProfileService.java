package com.example.ticket_router.service;

import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.repository.UserProfileRepository;

import org.springframework.stereotype.Service;

import com.example.ticket_router.exception.InvalidTicketException;

/**
 * Resolves the {@link UserProfile} (the ticket-ownership record) associated
 * with a given username, creating one on first use.
 * <p>
 * {@link UserProfile} is intentionally separate from the authentication
 * {@link com.example.ticket_router.entity.User} entity: it exists purely to
 * associate submitted tickets with a person, keyed by username.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    /**
     * @param userProfileRepository repository used to look up and persist user profiles
     */
    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Finds the existing {@link UserProfile} for the given name, or creates
     * and persists a new one if none exists yet.
     *
     * @param name the username to resolve a profile for
     * @return the existing or newly created profile
     * @throws InvalidTicketException if {@code name} is null or blank
     */
    public UserProfile getOrCreate(String name) {


    if(name == null || name.isBlank()) {

        throw new InvalidTicketException(
                "Username cannot be empty"
        );

    }


    return userProfileRepository.findByName(name)
            .orElseGet(() ->
                    userProfileRepository.save(
                            new UserProfile(name)
                    )
            );
}
}