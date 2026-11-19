package com.smartspend.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * User profile and preferences model.
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String email;
    private String displayName;
    private String collegeName;
    private String spendingMode = "Balanced Mode"; // "Easy Mode", "Balanced Mode", "Savings Focus"
    private String currency = "INR";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public UserProfile() {
    }

    public UserProfile(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getSpendingMode() {
        return spendingMode;
    }

    public void setSpendingMode(String spendingMode) {
        this.spendingMode = spendingMode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
