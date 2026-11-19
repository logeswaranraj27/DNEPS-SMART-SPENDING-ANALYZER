package com.smartspend.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Authoritative financial summary computed by the Java backend.
 */
public class FinancialSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount; // total income minus total expense for selected period
    private String period;        // e.g. "monthly", "weekly", "all"
    private String month;         // YYYY-MM
    private long transactionCount;

    public FinancialSummaryResponse() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpense = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
    }

    public FinancialSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense, String period, String month, long transactionCount) {
        this.totalIncome = totalIncome != null ? totalIncome.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.netAmount = this.totalIncome.subtract(this.totalExpense);
        this.period = period;
        this.month = month;
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
