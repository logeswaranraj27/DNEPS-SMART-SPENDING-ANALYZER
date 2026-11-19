package com.smartspend.service;

import com.smartspend.model.UserProfile;
import com.smartspend.repository.FirestoreRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service managing user identity and preference modes.
 */
@Service
public class UserService {

    private final FirestoreRepository repository;

    public UserService(FirestoreRepository repository) {
        this.repository = repository;
    }

    public UserProfile getProfile(String userId, String email, String name) {
        return repository.findUserProfile(userId)
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile(userId, email, name);
                    return repository.saveUserProfile(profile);
                });
    }

    public UserProfile updatePreferences(String userId, Map<String, Object> preferences) {
        UserProfile profile = repository.findUserProfile(userId)
                .orElseGet(() -> new UserProfile(userId, userId + "@smartspend.edu", "Student"));

        if (preferences.containsKey("spendingMode")) {
            profile.setSpendingMode(preferences.get("spendingMode").toString());
        }
        if (preferences.containsKey("collegeName")) {
            profile.setCollegeName(preferences.get("collegeName").toString());
        }
        if (preferences.containsKey("displayName")) {
            profile.setDisplayName(preferences.get("displayName").toString());
        }
        profile.setUpdatedAt(java.time.Instant.now());

        return repository.saveUserProfile(profile);
    }
}
