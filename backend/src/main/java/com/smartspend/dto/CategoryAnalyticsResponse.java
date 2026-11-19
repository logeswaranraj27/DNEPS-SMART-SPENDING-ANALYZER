package com.smartspend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for category-wise expense breakdown (used in Doughnut Chart).
 */
public class CategoryAnalyticsResponse {

    public static class CategoryItem {
        private String category;
        private BigDecimal amount;
        private double percentage;

        public CategoryItem() {}

        public CategoryItem(String category, BigDecimal amount, double percentage) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    private List<CategoryItem> categories = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String month;

    public CategoryAnalyticsResponse() {}

    public CategoryAnalyticsResponse(List<CategoryItem> categories, BigDecimal totalAmount, String month) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.month = month;
    }

    public List<CategoryItem> getCategories() { return categories; }
    public void setCategories(List<CategoryItem> categories) { this.categories = categories; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}
