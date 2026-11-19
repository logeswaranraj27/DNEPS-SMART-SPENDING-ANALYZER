package com.smartspend.service;

import com.smartspend.dto.AvoidableSavingsResponse;
import com.smartspend.dto.FinancialSummaryResponse;
import com.smartspend.dto.TransactionRequest;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2: Analytics & Avoidable Expense Calculations")
class AnalyticsServiceTest {

    private FirestoreRepository repository;
    private TransactionService transactionService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        repository = new FirestoreRepository();
        transactionService = new TransactionService(repository);
        analyticsService = new AnalyticsService(repository);
    }

    @Test
    @DisplayName("Should accurately compute authoritative net amount = total income minus total expense")
    void testFinancialSummaryNetCalculation() {
        // Income = ₹8,000
        transactionService.createTransaction("user1", new TransactionRequest(
                "income", "Allowance", BigDecimal.valueOf(8000), LocalDate.of(2026, 10, 1), "Parents", null
        ));

        // Expense 1 = ₹1,500
        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(1500), LocalDate.of(2026, 10, 5), "Meals", null
        ));

        // Expense 2 = ₹2,500
        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Education", BigDecimal.valueOf(2500), LocalDate.of(2026, 10, 10), "Books", null
        ));

        FinancialSummaryResponse summary = analyticsService.getFinancialSummary("user1", "monthly", "2026-10");
        assertEquals(0, BigDecimal.valueOf(8000).compareTo(summary.getTotalIncome()));
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(summary.getTotalExpense()));
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(summary.getNetAmount()));
    }

    @Test
    @DisplayName("Should calculate Section 6.D Where You Could Save with adjustable reduction targets (₹500 spending * 30% = ₹150 saved, target ₹350)")
    void testWhereYouCouldSaveCalculations() {
        // Record Snacks & Drinks = ₹500
        List<TransactionItem> items = List.of(
                new TransactionItem("Snacks", BigDecimal.valueOf(300)),
                new TransactionItem("Drinks", BigDecimal.valueOf(200))
        );

        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(500), LocalDate.of(2026, 10, 8), "Canteen break", items
        ));

        // 30% reduction target
        AvoidableSavingsResponse resp = analyticsService.getAvoidableSavingsOpportunities("user1", "2026-10", 30);
        assertFalse(resp.getOpportunities().isEmpty());

        AvoidableSavingsResponse.SavingsOpportunity opp = resp.getOpportunities().get(0);
        assertEquals(0, BigDecimal.valueOf(500).compareTo(opp.getCurrentSpending()));
        assertEquals(0, BigDecimal.valueOf(150).compareTo(opp.getPotentialSavings())); // 500 * 0.30
        assertEquals(0, BigDecimal.valueOf(350).compareTo(opp.getSuggestedSpendingTarget())); // 500 - 150
    }
}
