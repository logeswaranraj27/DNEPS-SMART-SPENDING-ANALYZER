package com.smartspend.service;

import com.smartspend.dto.AiQuestionRequest;
import com.smartspend.dto.AiQuestionResponse;
import com.smartspend.dto.TransactionRequest;
import com.smartspend.dto.WeeklyReviewResponse;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 3: DNEPS AI & Weekly Review Tests")
class DnepsAIServiceTest {

    private FirestoreRepository repository;
    private TransactionService transactionService;
    private DnepsAIService dnepsAIService;

    @BeforeEach
    void setUp() {
        repository = new FirestoreRepository();
        transactionService = new TransactionService(repository);
        dnepsAIService = new DnepsAIService(repository);
    }

    @Test
    @DisplayName("Should answer financial question using actual recorded transactions in demonstration mode when live API key is unconfigured")
    void testDnepsAIDemoModeAnswers() {
        // Record ₹320 in snacks (Chips ₹60, Cookies ₹180, Samosa ₹80)
        List<TransactionItem> items = List.of(
                new TransactionItem("Chips", BigDecimal.valueOf(60)),
                new TransactionItem("Cookies", BigDecimal.valueOf(180)),
                new TransactionItem("Samosa", BigDecimal.valueOf(80))
        );

        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(320), LocalDate.now(), "Canteen snacks", items
        ));

        AiQuestionRequest req = new AiQuestionRequest(
                "How much did I spend on snacks this week?", Map.of("mode", "Balanced Mode")
        );

        AiQuestionResponse resp = dnepsAIService.ask("user1", req);
        assertNotNull(resp);
        assertTrue(resp.isDemo(), "Should honestly indicate demo mode when API key is not configured");
        assertTrue(resp.getAnswer().contains("320.00"), "Response should reflect the exact ₹320 snack spending computed by Java");
        assertTrue(resp.getAnswer().contains("Balanced Mode"));
    }

    @Test
    @DisplayName("Should compute 7-day Weekly Spending Review comparing current week with previous week")
    void testWeeklySpendingReview() {
        LocalDate today = LocalDate.now();

        // Current week: ₹500
        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(500), today.minusDays(2), "Canteen", null
        ));

        // Previous week: ₹800
        transactionService.createTransaction("user1", new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(800), today.minusDays(10), "Prior week", null
        ));

        WeeklyReviewResponse review = dnepsAIService.getWeeklySpendingReview("user1");
        assertNotNull(review);
        assertEquals(0, BigDecimal.valueOf(500).compareTo(review.getTotalWeeklyExpense()));
        assertEquals(0, BigDecimal.valueOf(800).compareTo(review.getPreviousWeekExpense()));
        assertEquals(0, BigDecimal.valueOf(-300).compareTo(review.getDifference())); // Spent 300 less
        assertEquals("Food", review.getTopCategory());
        assertFalse(review.getInsights().isEmpty());
    }
}
