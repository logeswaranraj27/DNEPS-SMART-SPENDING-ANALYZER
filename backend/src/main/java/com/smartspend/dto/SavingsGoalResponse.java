package com.smartspend.dto;

import com.smartspend.model.SavingsGoal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DTO for savings goal details with Java-calculated progress and savings rate.
 */
public class SavingsGoalResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal remainingAmount;
    private double progressPercentage;
    private LocalDate targetDate;
    private Long daysRemaining;
    private BigDecimal suggestedWeeklySavingsRate;
    private String status; // "In Progress", "Fully Funded", "Overdue"

    public SavingsGoalResponse() {
    }

    public SavingsGoalResponse(SavingsGoal goal) {
        if (goal != null) {
            this.id = goal.getId();
            this.name = goal.getName();
            this.description = goal.getDescription();
            this.targetAmount = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
            this.currentAmount = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
            this.targetDate = goal.getTargetDate();

            calculateMetrics();
        }
    }

    private void calculateMetrics() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.remainingAmount = BigDecimal.ZERO;
            this.progressPercentage = 100.0;
            this.status = "Fully Funded";
            this.suggestedWeeklySavingsRate = BigDecimal.ZERO;
            return;
        }

        // Remaining amount = target - current
        BigDecimal diff = targetAmount.subtract(currentAmount);
        this.remainingAmount = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;

        // Progress percentage
        BigDecimal pct = currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        this.progressPercentage = Math.min(pct.doubleValue(), 100.0);

        boolean isFunded = currentAmount.compareTo(targetAmount) >= 0;

        if (isFunded) {
            this.status = "Fully Funded";
            this.suggestedWeeklySavingsRate = BigDecimal.ZERO;
            if (targetDate != null) {
                this.daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
            }
            return;
        }

        // Date calculations
        if (targetDate != null) {
            LocalDate today = LocalDate.now();
            this.daysRemaining = ChronoUnit.DAYS.between(today, targetDate);

            if (this.daysRemaining < 0) {
                this.status = "Overdue";
                this.suggestedWeeklySavingsRate = this.remainingAmount;
            } else if (this.daysRemaining == 0) {
                this.status = "Target is Today";
                this.suggestedWeeklySavingsRate = this.remainingAmount;
            } else {
                this.status = "In Progress";
                long weeks = Math.max(1, (long) Math.ceil((double) this.daysRemaining / 7.0));
                this.suggestedWeeklySavingsRate = this.remainingAmount
                        .divide(BigDecimal.valueOf(weeks), 2, RoundingMode.HALF_UP);
            }
        } else {
            this.status = "In Progress";
            this.daysRemaining = null;
            this.suggestedWeeklySavingsRate = BigDecimal.ZERO;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public BigDecimal getSuggestedWeeklySavingsRate() {
        return suggestedWeeklySavingsRate;
    }

    public void setSuggestedWeeklySavingsRate(BigDecimal suggestedWeeklySavingsRate) {
        this.suggestedWeeklySavingsRate = suggestedWeeklySavingsRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
