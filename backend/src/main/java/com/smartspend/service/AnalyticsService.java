package com.smartspend.service;

import com.smartspend.dto.AvoidableSavingsResponse;
import com.smartspend.dto.CategoryAnalyticsResponse;
import com.smartspend.dto.FinancialSummaryResponse;
import com.smartspend.dto.MonthlyComparisonResponse;
import com.smartspend.dto.SpendingTrendsResponse;
import com.smartspend.model.Transaction;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 2 Service: Spending Analytics & Avoidable Expense Calculations.
 */
@Service
public class AnalyticsService {

    private final FirestoreRepository repository;

    public AnalyticsService(FirestoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Compute authoritative net amount, income, and expense totals for the selected period.
     */
    public FinancialSummaryResponse getFinancialSummary(String userId, String period, String month) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<Transaction> filtered = transactions.stream()
                .filter(tx -> {
                    if (month != null && !month.trim().isEmpty()) {
                        return tx.getDate() != null && tx.getDate().format(monthFormatter).equals(month);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        BigDecimal totalIncome = filtered.stream()
                .filter(tx -> "income".equalsIgnoreCase(tx.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = filtered.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinancialSummaryResponse(totalIncome, totalExpense, period, month, filtered.size());
    }

    /**
     * Category-wise breakdown of expenses for Doughnut chart.
     */
    public CategoryAnalyticsResponse getCategoryAnalytics(String userId, String month) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<Transaction> expenses = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> {
                    if (month != null && !month.trim().isEmpty()) {
                        return tx.getDate() != null && tx.getDate().format(monthFormatter).equals(month);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        BigDecimal total = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Transaction tx : expenses) {
            String cat = tx.getCategory() != null ? tx.getCategory() : "Other expenses";
            byCategory.put(cat, byCategory.getOrDefault(cat, BigDecimal.ZERO).add(tx.getAmount()));
        }

        List<CategoryAnalyticsResponse.CategoryItem> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
            double pct = 0.0;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                pct = entry.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, RoundingMode.HALF_UP).doubleValue();
            }
            items.add(new CategoryAnalyticsResponse.CategoryItem(entry.getKey(), entry.getValue(), pct));
        }

        items.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return new CategoryAnalyticsResponse(items, total, month);
    }

    /**
     * Daily spending points over the specified number of days for Line chart.
     */
    public SpendingTrendsResponse getSpendingTrends(String userId, int days) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        Map<LocalDate, BigDecimal> dailyMap = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            dailyMap.put(startDate.plusDays(i), BigDecimal.ZERO);
        }

        for (Transaction tx : transactions) {
            if ("expense".equalsIgnoreCase(tx.getType()) && tx.getDate() != null && !tx.getDate().isBefore(startDate)) {
                dailyMap.computeIfPresent(tx.getDate(), (k, v) -> v.add(tx.getAmount()));
            }
        }

        List<SpendingTrendsResponse.TrendPoint> points = dailyMap.entrySet().stream()
                .map(e -> new SpendingTrendsResponse.TrendPoint(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new SpendingTrendsResponse(points, "daily");
    }

    /**
     * Monthly comparison (Income vs Expense) for Bar chart.
     */
    public MonthlyComparisonResponse getMonthlyComparison(String userId, Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<Transaction> transactions = repository.findTransactionsByUser(userId);

        Map<String, MonthlyComparisonResponse.MonthStat> statsMap = new TreeMap<>();
        for (int m = 1; m <= 12; m++) {
            String monthKey = String.format("%04d-%02d", targetYear, m);
            String monthName = java.time.Month.of(m).name().substring(0, 3) + " " + targetYear;
            statsMap.put(monthKey, new MonthlyComparisonResponse.MonthStat(monthKey, monthName, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Transaction tx : transactions) {
            if (tx.getDate() != null && tx.getDate().getYear() == targetYear) {
                String key = tx.getDate().format(formatter);
                MonthlyComparisonResponse.MonthStat stat = statsMap.get(key);
                if (stat != null) {
                    if ("income".equalsIgnoreCase(tx.getType())) {
                        stat.setIncome(stat.getIncome().add(tx.getAmount()));
                    } else if ("expense".equalsIgnoreCase(tx.getType())) {
                        stat.setExpense(stat.getExpense().add(tx.getAmount()));
                    }
                    stat.setNet(stat.getIncome().subtract(stat.getExpense()));
                }
            }
        }

        return new MonthlyComparisonResponse(new ArrayList<>(statsMap.values()));
    }

    /**
     * Section 6.D: Avoidable Expense Analysis ("Where You Could Save").
     * Analyzes recorded item-level expenses, categories, and spending patterns.
     */
    public AvoidableSavingsResponse getAvoidableSavingsOpportunities(String userId, String month, int reductionPercent) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<Transaction> expenses = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> {
                    if (month != null && !month.trim().isEmpty()) {
                        return tx.getDate() != null && tx.getDate().format(monthFormatter).equals(month);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<AvoidableSavingsResponse.SavingsOpportunity> opportunities = new ArrayList<>();

        // 1. Group item-level purchases (e.g. Snacks, Drinks, Cool Drinks, Chips)
        Map<String, BigDecimal> itemGroups = new HashMap<>();
        for (Transaction tx : expenses) {
            if (tx.getItems() != null) {
                for (TransactionItem item : tx.getItems()) {
                    String name = item.getName() != null ? item.getName().toLowerCase().trim() : "";
                    if (name.contains("snack") || name.contains("chips") || name.contains("drink") ||
                        name.contains("tea") || name.contains("coffee") || name.contains("chocolate") ||
                        name.contains("biscuit") || name.contains("samosa") || name.contains("maggi") ||
                        name.contains("fast food") || name.contains("burger")) {
                        itemGroups.put("Snacks, Drinks & Quick Bites",
                                itemGroups.getOrDefault("Snacks, Drinks & Quick Bites", BigDecimal.ZERO).add(item.getAmount()));
                    }
                }
            }
        }

        if (itemGroups.containsKey("Snacks, Drinks & Quick Bites")) {
            BigDecimal spend = itemGroups.get("Snacks, Drinks & Quick Bites");
            if (spend.compareTo(BigDecimal.valueOf(100)) > 0) {
                opportunities.add(new AvoidableSavingsResponse.SavingsOpportunity(
                        "Snacks, Drinks & Quick Bites",
                        spend,
                        reductionPercent,
                        "Recorded from your canteen itemized purchases. Setting a small reduction target allows you to redirect funds to your active savings goals while maintaining your routine."
                ));
            }
        }

        // 2. Check Entertainment or Shopping category
        BigDecimal entertainmentSpend = expenses.stream()
                .filter(tx -> "Entertainment".equalsIgnoreCase(tx.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (entertainmentSpend.compareTo(BigDecimal.valueOf(250)) > 0) {
            opportunities.add(new AvoidableSavingsResponse.SavingsOpportunity(
                    "Entertainment & Weekend Outings",
                    entertainmentSpend,
                    reductionPercent,
                    "Flexible social outings and streaming subscriptions. Exploring student discounts or shared group activities can unlock easy weekly savings."
            ));
        }

        BigDecimal shoppingSpend = expenses.stream()
                .filter(tx -> "Shopping".equalsIgnoreCase(tx.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (shoppingSpend.compareTo(BigDecimal.valueOf(500)) > 0) {
            opportunities.add(new AvoidableSavingsResponse.SavingsOpportunity(
                    "Shopping & Personal Discretionary Purchases",
                    shoppingSpend,
                    reductionPercent,
                    "Non-essential items and impulse shopping. Delaying discretionary purchases by 48 hours is a proven way to preserve student cash flow."
            ));
        }

        return new AvoidableSavingsResponse(opportunities);
    }
}
