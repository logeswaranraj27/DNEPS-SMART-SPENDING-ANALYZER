package com.smartspend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Section 7.F: "Your Weekly Spending Review".
 * Calculated authoritatively in Java for the last 7 days compared with previous 7 days.
 */
public class WeeklyReviewResponse {
    private BigDecimal totalWeeklyExpense;
    private BigDecimal previousWeekExpense;
    private BigDecimal difference; // positive = spent more, negative = spent less
    private String topCategory;
    private BigDecimal topCategoryAmount;
    private String highestCostItem;
    private BigDecimal highestItemAmount;
    private List<String> increasedCategories = new ArrayList<>();
    private List<String> insights = new ArrayList<>();
    private BigDecimal suggestedNextWeekBudget;

    public WeeklyReviewResponse() {
        this.totalWeeklyExpense = BigDecimal.ZERO;
        this.previousWeekExpense = BigDecimal.ZERO;
        this.difference = BigDecimal.ZERO;
    }

    public BigDecimal getTotalWeeklyExpense() { return totalWeeklyExpense; }
    public void setTotalWeeklyExpense(BigDecimal totalWeeklyExpense) { this.totalWeeklyExpense = totalWeeklyExpense; }
    public BigDecimal getPreviousWeekExpense() { return previousWeekExpense; }
    public void setPreviousWeekExpense(BigDecimal previousWeekExpense) { this.previousWeekExpense = previousWeekExpense; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public String getTopCategory() { return topCategory; }
    public void setTopCategory(String topCategory) { this.topCategory = topCategory; }
    public BigDecimal getTopCategoryAmount() { return topCategoryAmount; }
    public void setTopCategoryAmount(BigDecimal topCategoryAmount) { this.topCategoryAmount = topCategoryAmount; }
    public String getHighestCostItem() { return highestCostItem; }
    public void setHighestCostItem(String highestCostItem) { this.highestCostItem = highestCostItem; }
    public BigDecimal getHighestItemAmount() { return highestItemAmount; }
    public void setHighestItemAmount(BigDecimal highestItemAmount) { this.highestItemAmount = highestItemAmount; }
    public List<String> getIncreasedCategories() { return increasedCategories; }
    public void setIncreasedCategories(List<String> increasedCategories) { this.increasedCategories = increasedCategories; }
    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }
    public BigDecimal getSuggestedNextWeekBudget() { return suggestedNextWeekBudget; }
    public void setSuggestedNextWeekBudget(BigDecimal suggestedNextWeekBudget) { this.suggestedNextWeekBudget = suggestedNextWeekBudget; }
}
