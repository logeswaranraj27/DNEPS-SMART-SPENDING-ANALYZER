package com.smartspend.dto;

import com.smartspend.model.Transaction;
import com.smartspend.model.TransactionItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for outgoing Transaction details.
 */
public class TransactionResponse {
    private String id;
    private String type;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private List<TransactionItem> items;
    private Instant createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(Transaction tx) {
        if (tx != null) {
            this.id = tx.getId();
            this.type = tx.getType();
            this.category = tx.getCategory();
            this.amount = tx.getAmount();
            this.date = tx.getDate();
            this.description = tx.getDescription();
            this.items = tx.getItems();
            this.createdAt = tx.getCreatedAt();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        this.amount = amount;
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
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
