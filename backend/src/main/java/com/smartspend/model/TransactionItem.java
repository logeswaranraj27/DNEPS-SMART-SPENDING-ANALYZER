package com.smartspend.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents an individual purchased item within an expense transaction.
 * (e.g. Lunch: ₹150, Chips: ₹60, Cool drink: ₹70, Chocolate: ₹70).
 */
public class TransactionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private BigDecimal amount;

    public TransactionItem() {
    }

    public TransactionItem(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public TransactionItem(String name, double amount) {
        this(name, BigDecimal.valueOf(amount));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
