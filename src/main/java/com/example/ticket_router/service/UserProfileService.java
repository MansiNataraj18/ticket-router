package com.example.ticket_router.service;

import com.example.ticket_router.entity.UserProfile;
import com.example.ticket_router.repository.UserProfileRepository;

import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile getOrCreate(String name) {

        return userProfileRepository.findByName(name)
                .orElseGet(() ->
                        userProfileRepository.save(
                                new UserProfile(name)
                        )
                );
    }
}