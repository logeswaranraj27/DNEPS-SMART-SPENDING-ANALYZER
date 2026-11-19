package com.smartspend.service;

import com.smartspend.dto.BudgetRequest;
import com.smartspend.dto.BudgetResponse;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.Budget;
import com.smartspend.model.Transaction;
import com.smartspend.repository.FirestoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 2 Service: Budget Planning & Calculations.
 * Calculates spent amounts, remaining budgets, and 4-tier status thresholds.
 */
@Service
public class BudgetService {

    private final FirestoreRepository repository;

    public BudgetService(FirestoreRepository repository) {
        this.repository = repository;
    }

    public BudgetResponse createBudget(String userId, BudgetRequest request) {
        validateBudgetRequest(request);

        // Prevent duplicate budgets for the same user, month, and category
        List<Budget> existing = repository.findBudgetsByUser(userId, request.getMonth());
        boolean duplicate = existing.stream().anyMatch(b -> b.getCategory().equalsIgnoreCase(request.getCategory().trim()));
        if (duplicate) {
            throw new BadRequestException("A budget limit for category '" + request.getCategory() +
                    "' already exists for month " + request.getMonth());
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory(request.getCategory().trim());
        budget.setLimitAmount(request.getLimitAmount().setScale(2, RoundingMode.HALF_UP));
        budget.setMonth(request.getMonth());

        Budget saved = repository.saveBudget(userId, budget);
        BigDecimal spent = calculateSpentForBudget(userId, saved.getCategory(), saved.getMonth());
        return new BudgetResponse(saved, spent);
    }

    public BudgetResponse updateBudget(String userId, String budgetId, BudgetRequest request) {
        validateBudgetRequest(request);

        Budget budget = repository.findBudgetById(userId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));

        // Check if category or month changed and collides with another budget
        if (!budget.getCategory().equalsIgnoreCase(request.getCategory().trim()) || !budget.getMonth().equals(request.getMonth())) {
            List<Budget> existing = repository.findBudgetsByUser(userId, request.getMonth());
            boolean duplicate = existing.stream()
                    .anyMatch(b -> !b.getId().equals(budgetId) && b.getCategory().equalsIgnoreCase(request.getCategory().trim()));
            if (duplicate) {
                throw new BadRequestException("Another budget limit for category '" + request.getCategory() +
                        "' already exists for month " + request.getMonth());
            }
        }

        budget.setCategory(request.getCategory().trim());
        budget.setLimitAmount(request.getLimitAmount().setScale(2, RoundingMode.HALF_UP));
        budget.setMonth(request.getMonth());
        budget.setUpdatedAt(java.time.Instant.now());

        Budget updated = repository.saveBudget(userId, budget);
        BigDecimal spent = calculateSpentForBudget(userId, updated.getCategory(), updated.getMonth());
        return new BudgetResponse(updated, spent);
    }

    public List<BudgetResponse> getBudgets(String userId, String month) {
        List<Budget> budgets = repository.findBudgetsByUser(userId, month);
        return budgets.stream()
                .map(b -> {
                    BigDecimal spent = calculateSpentForBudget(userId, b.getCategory(), b.getMonth());
                    return new BudgetResponse(b, spent);
                })
                .collect(Collectors.toList());
    }

    public BudgetResponse getBudgetById(String userId, String budgetId) {
        Budget budget = repository.findBudgetById(userId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));
        BigDecimal spent = calculateSpentForBudget(userId, budget.getCategory(), budget.getMonth());
        return new BudgetResponse(budget, spent);
    }

    public void deleteBudget(String userId, String budgetId) {
        boolean deleted = repository.deleteBudget(userId, budgetId);
        if (!deleted) {
            throw new ResourceNotFoundException("Budget not found with id: " + budgetId);
        }
    }

    /**
     * Compute authoritative spent amount for this category and month in Java.
     */
    public BigDecimal calculateSpentForBudget(String userId, String category, String month) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        boolean isOverall = "overall".equalsIgnoreCase(category) || "overall budget".equalsIgnoreCase(category);

        return transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && tx.getDate().format(formatter).equals(month))
                .filter(tx -> isOverall || (tx.getCategory() != null && tx.getCategory().equalsIgnoreCase(category)))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateBudgetRequest(BudgetRequest request) {
        if (request.getLimitAmount() == null || request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Budget limit must be a positive number.");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Budget category is required.");
        }
        if (request.getMonth() == null || !request.getMonth().matches("^\\d{4}-\\d{2}$")) {
            throw new BadRequestException("Applicable month must be in YYYY-MM format (e.g., 2026-10).");
        }
    }
}
