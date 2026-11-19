package com.smartspend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for monthly comparisons across months of the year.
 */
public class MonthlyComparisonResponse {

    public static class MonthStat {
        private String month;     // YYYY-MM
        private String monthName; // e.g., "Oct 2026"
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;

        public MonthStat() {}

        public MonthStat(String month, String monthName, BigDecimal income, BigDecimal expense) {
            this.month = month;
            this.monthName = monthName;
            this.income = income != null ? income : BigDecimal.ZERO;
            this.expense = expense != null ? expense : BigDecimal.ZERO;
            this.net = this.income.subtract(this.expense);
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }
        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }
        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }
        public BigDecimal getNet() { return net; }
        public void setNet(BigDecimal net) { this.net = net; }
    }

    private List<MonthStat> months = new ArrayList<>();

    public MonthlyComparisonResponse() {}

    public MonthlyComparisonResponse(List<MonthStat> months) {
        this.months = months != null ? months : new ArrayList<>();
    }

    public List<MonthStat> getMonths() { return months; }
    public void setMonths(List<MonthStat> months) { this.months = months; }
}
