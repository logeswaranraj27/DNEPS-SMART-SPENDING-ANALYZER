package com.smartspend.dto;

import com.smartspend.model.Budget;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO for budget details with Java-calculated utilization and status.
 */
public class BudgetResponse {
    private String id;
    private String category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double utilizationPercentage;
    private String status; // "Within budget", "Approaching limit", "Budget fully used", "Budget exceeded"
    private String month;

    public BudgetResponse() {
    }

    public BudgetResponse(Budget budget, BigDecimal spentAmount) {
        if (budget != null) {
            this.id = budget.getId();
            this.category = budget.getCategory();
            this.limitAmount = budget.getLimitAmount() != null ? budget.getLimitAmount() : BigDecimal.ZERO;
            this.spentAmount = spentAmount != null ? spentAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.month = budget.getMonth();

            calculateMetrics();
        }
    }

    private void calculateMetrics() {
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.remainingAmount = BigDecimal.ZERO;
            this.utilizationPercentage = 0.0;
            this.status = "Within budget";
            return;
        }

        // remaining = limit - spent
        BigDecimal diff = limitAmount.subtract(spentAmount);
        this.remainingAmount = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;

        // utilization percentage = (spent / limit) * 100
        BigDecimal pct = spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
        this.utilizationPercentage = pct.doubleValue();

        // 4-tier initial budget states
        if (this.utilizationPercentage < 75.0) {
            this.status = "Within budget";
        } else if (this.utilizationPercentage < 100.0) {
            this.status = "Approaching limit";
        } else if (Math.abs(this.utilizationPercentage - 100.0) < 0.001) {
            this.status = "Budget fully used";
        } else {
            this.status = "Budget exceeded";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.limitAmount = limitAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(double utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
