package com.example.ticket_router.service;

import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.repository.UserProfileRepository;

import org.springframework.stereotype.Service;

import com.example.ticket_router.exception.InvalidTicketException;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

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