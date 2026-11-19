package com.smartspend.controller;

import com.smartspend.model.UserProfile;
import com.smartspend.security.AuthenticatedUser;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for User Profile and Preferences.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile() {
        String uid = SecurityUtils.getAuthenticatedUid();
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        String email = user != null ? user.getEmail() : uid + "@smartspend.edu";
        String name = user != null ? user.getName() : "Student";

        UserProfile profile = userService.getProfile(uid, email, name);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserProfile> updatePreferences(@RequestBody Map<String, Object> preferences) {
        String uid = SecurityUtils.getAuthenticatedUid();
        UserProfile updated = userService.updatePreferences(uid, preferences);
        return ResponseEntity.ok(updated);
    }
}
