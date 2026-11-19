package com.smartspend.service;

import com.smartspend.dto.BudgetRequest;
import com.smartspend.dto.BudgetResponse;
import com.smartspend.dto.TransactionRequest;
import com.smartspend.exception.BadRequestException;
import com.smartspend.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 2: Budget Planning & 4-Tier Warning Tests")
class BudgetServiceTest {

    private FirestoreRepository repository;
    private BudgetService budgetService;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        repository = new FirestoreRepository();
        budgetService = new BudgetService(repository);
        transactionService = new TransactionService(repository);
    }

    @Test
    @DisplayName("Should calculate 'Within budget' when spending is below 75%")
    void testWithinBudgetStatus() {
        // Budget limit = 1000 for month 2026-10
        BudgetRequest bReq = new BudgetRequest("Food", BigDecimal.valueOf(1000), "2026-10");
        budgetService.createBudget("user1", bReq);

        // Record expense = 500 (50%)
        TransactionRequest txReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(500), LocalDate.of(2026, 10, 5), "Lunch", null
        );
        transactionService.createTransaction("user1", txReq);

        BudgetResponse resp = budgetService.getBudgets("user1", "2026-10").get(0);
        assertEquals(50.0, resp.getUtilizationPercentage());
        assertEquals("Within budget", resp.getStatus());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(resp.getRemainingAmount()));
    }

    @Test
    @DisplayName("Should trigger 'Approaching limit' when spending is between 75% and 99%")
    void testApproachingLimitStatus() {
        BudgetRequest bReq = new BudgetRequest("Food", BigDecimal.valueOf(1000), "2026-10");
        budgetService.createBudget("user1", bReq);

        // Record expense = 800 (80%)
        TransactionRequest txReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(800), LocalDate.of(2026, 10, 10), "Groceries", null
        );
        transactionService.createTransaction("user1", txReq);

        BudgetResponse resp = budgetService.getBudgets("user1", "2026-10").get(0);
        assertEquals(80.0, resp.getUtilizationPercentage());
        assertEquals("Approaching limit", resp.getStatus());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(resp.getRemainingAmount()));
    }

    @Test
    @DisplayName("Should detect 'Budget fully used' at exactly 100%")
    void testBudgetFullyUsedStatus() {
        BudgetRequest bReq = new BudgetRequest("Food", BigDecimal.valueOf(1000), "2026-10");
        budgetService.createBudget("user1", bReq);

        TransactionRequest txReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(1000), LocalDate.of(2026, 10, 15), "Dinner", null
        );
        transactionService.createTransaction("user1", txReq);

        BudgetResponse resp = budgetService.getBudgets("user1", "2026-10").get(0);
        assertEquals(100.0, resp.getUtilizationPercentage());
        assertEquals("Budget fully used", resp.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.getRemainingAmount()));
    }

    @Test
    @DisplayName("Should trigger 'Budget exceeded' warning when spending is over 100%")
    void testBudgetExceededStatus() {
        BudgetRequest bReq = new BudgetRequest("Food", BigDecimal.valueOf(1000), "2026-10");
        budgetService.createBudget("user1", bReq);

        TransactionRequest txReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(1250), LocalDate.of(2026, 10, 20), "Feast", null
        );
        transactionService.createTransaction("user1", txReq);

        BudgetResponse resp = budgetService.getBudgets("user1", "2026-10").get(0);
        assertEquals(125.0, resp.getUtilizationPercentage());
        assertEquals("Budget exceeded", resp.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.getRemainingAmount()));
    }

    @Test
    @DisplayName("Should prevent accidental duplicate budget for same month and category")
    void testPreventDuplicateBudgets() {
        BudgetRequest bReq = new BudgetRequest("Entertainment", BigDecimal.valueOf(2000), "2026-10");
        budgetService.createBudget("user1", bReq);

        // Attempt second budget for Entertainment in 2026-10
        BadRequestException ex = assertThrows(BadRequestException.class, () -> budgetService.createBudget("user1", bReq));
        assertTrue(ex.getMessage().contains("already exists"));
    }
}
