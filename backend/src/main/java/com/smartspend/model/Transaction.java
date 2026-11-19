package com.smartspend.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction entity for income and expenses.
 * Enforces authoritative user UID isolation and itemized receipts.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId; // Authenticated user's UID (derived from Firebase token)
    private String type;   // "income" or "expense"
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private List<TransactionItem> items = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Transaction() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Transaction(String id, String userId, String type, String category, BigDecimal amount, LocalDate date, String description) {
        this.id = id;
        this.userId = userId;
        this.type = type != null ? type.toLowerCase() : "expense";
        this.category = category;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.date = date != null ? date : LocalDate.now();
        this.description = description;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : "expense";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TransactionItem> getItems() {
        return items;
    }

    public void setItems(List<TransactionItem> items) {
        this.items = items != null ? items : new ArrayList<>();
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
