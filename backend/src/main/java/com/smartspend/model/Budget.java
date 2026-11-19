package com.smartspend.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Monthly budget spending limit for a specific category or overall spending.
 */
public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String category; // e.g. "Food", "Entertainment", or "Overall Budget"
    private BigDecimal limitAmount;
    private String month;    // YYYY-MM format (e.g. "2026-10")
    private Instant createdAt;
    private Instant updatedAt;

    public Budget() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Budget(String id, String userId, String category, BigDecimal limitAmount, String month) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount != null ? limitAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.month = month;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount != null ? limitAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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
