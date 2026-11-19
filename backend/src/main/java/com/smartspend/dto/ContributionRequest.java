package com.smartspend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for contributing / depositing funds towards a savings goal.
 */
public class ContributionRequest {

    @NotNull(message = "Contribution amount is required")
    @Positive(message = "Contribution amount must be greater than zero")
    private BigDecimal amount;

    public ContributionRequest() {
    }

    public ContributionRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
