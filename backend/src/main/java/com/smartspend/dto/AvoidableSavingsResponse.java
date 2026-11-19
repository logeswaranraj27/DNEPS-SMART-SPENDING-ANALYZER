package com.smartspend.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Section 6.D: Avoidable Expense Analysis ("Where You Could Save").
 * Calculates suggested spending targets and potential savings using Java.
 */
public class AvoidableSavingsResponse {

    public static class SavingsOpportunity {
        private String itemOrCategory;
        private BigDecimal currentSpending;
        private BigDecimal suggestedSpendingTarget;
        private BigDecimal potentialSavings;
        private int reductionPercent; // e.g. 30%
        private String explanation;
        private String historicalNote;

        public SavingsOpportunity() {}

        public SavingsOpportunity(String itemOrCategory, BigDecimal currentSpending, int reductionPercent, String explanation) {
            this.itemOrCategory = itemOrCategory;
            this.currentSpending = currentSpending != null ? currentSpending.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.reductionPercent = reductionPercent;
            this.explanation = explanation;

            calculate(reductionPercent);
        }

        public void calculate(int reductionPercent) {
            this.reductionPercent = reductionPercent;
            BigDecimal factor = BigDecimal.valueOf(reductionPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            this.potentialSavings = currentSpending.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            this.suggestedSpendingTarget = currentSpending.subtract(this.potentialSavings).setScale(2, RoundingMode.HALF_UP);
        }

        public String getItemOrCategory() { return itemOrCategory; }
        public void setItemOrCategory(String itemOrCategory) { this.itemOrCategory = itemOrCategory; }
        public BigDecimal getCurrentSpending() { return currentSpending; }
        public void setCurrentSpending(BigDecimal currentSpending) { this.currentSpending = currentSpending; }
        public BigDecimal getSuggestedSpendingTarget() { return suggestedSpendingTarget; }
        public void setSuggestedSpendingTarget(BigDecimal suggestedSpendingTarget) { this.suggestedSpendingTarget = suggestedSpendingTarget; }
        public BigDecimal getPotentialSavings() { return potentialSavings; }
        public void setPotentialSavings(BigDecimal potentialSavings) { this.potentialSavings = potentialSavings; }
        public int getReductionPercent() { return reductionPercent; }
        public void setReductionPercent(int reductionPercent) { this.reductionPercent = reductionPercent; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getHistoricalNote() { return historicalNote; }
        public void setHistoricalNote(String historicalNote) { this.historicalNote = historicalNote; }
    }

    private List<SavingsOpportunity> opportunities = new ArrayList<>();
    private BigDecimal totalPotentialSavings = BigDecimal.ZERO;

    public AvoidableSavingsResponse() {}

    public AvoidableSavingsResponse(List<SavingsOpportunity> opportunities) {
        this.opportunities = opportunities != null ? opportunities : new ArrayList<>();
        this.totalPotentialSavings = this.opportunities.stream()
                .map(SavingsOpportunity::getPotentialSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<SavingsOpportunity> getOpportunities() { return opportunities; }
    public void setOpportunities(List<SavingsOpportunity> opportunities) { this.opportunities = opportunities; }
    public BigDecimal getTotalPotentialSavings() { return totalPotentialSavings; }
    public void setTotalPotentialSavings(BigDecimal totalPotentialSavings) { this.totalPotentialSavings = totalPotentialSavings; }
}
