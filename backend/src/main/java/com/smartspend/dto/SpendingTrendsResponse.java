package com.smartspend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for spending trends over daily or weekly points.
 */
public class SpendingTrendsResponse {

    public static class TrendPoint {
        private LocalDate date;
        private BigDecimal amount;

        public TrendPoint() {}

        public TrendPoint(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    private List<TrendPoint> points = new ArrayList<>();
    private String period;

    public SpendingTrendsResponse() {}

    public SpendingTrendsResponse(List<TrendPoint> points, String period) {
        this.points = points != null ? points : new ArrayList<>();
        this.period = period;
    }

    public List<TrendPoint> getPoints() { return points; }
    public void setPoints(List<TrendPoint> points) { this.points = points; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
