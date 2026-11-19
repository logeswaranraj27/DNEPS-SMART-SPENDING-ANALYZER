package com.smartspend.service;

import com.smartspend.dto.SavingsGoalRequest;
import com.smartspend.dto.SavingsGoalResponse;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.SavingsGoal;
import com.smartspend.repository.FirestoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 3 Service: Savings Goals & Progress Calculations.
 */
@Service
public class SavingsGoalService {

    private final FirestoreRepository repository;

    public SavingsGoalService(FirestoreRepository repository) {
        this.repository = repository;
    }

    public SavingsGoalResponse createGoal(String userId, SavingsGoalRequest request) {
        validateSavingsGoalRequest(request);

        SavingsGoal goal = new SavingsGoal();
        goal.setUserId(userId);
        goal.setName(request.getName().trim());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount().setScale(2, RoundingMode.HALF_UP));
        goal.setCurrentAmount(request.getCurrentAmount() != null 
                ? request.getCurrentAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        goal.setTargetDate(request.getTargetDate());

        SavingsGoal saved = repository.saveSavingsGoal(userId, goal);
        return new SavingsGoalResponse(saved);
    }

    public SavingsGoalResponse updateGoal(String userId, String goalId, SavingsGoalRequest request) {
        validateSavingsGoalRequest(request);

        SavingsGoal goal = repository.findSavingsGoalById(userId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + goalId));

        goal.setName(request.getName().trim());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount().setScale(2, RoundingMode.HALF_UP));
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount().setScale(2, RoundingMode.HALF_UP));
        }
        goal.setTargetDate(request.getTargetDate());
        goal.setUpdatedAt(java.time.Instant.now());

        SavingsGoal updated = repository.saveSavingsGoal(userId, goal);
        return new SavingsGoalResponse(updated);
    }

    public SavingsGoalResponse contribute(String userId, String goalId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Contribution amount must be greater than zero.");
        }

        SavingsGoal goal = repository.findSavingsGoalById(userId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + goalId));

        BigDecimal newAmount = goal.getCurrentAmount().add(amount).setScale(2, RoundingMode.HALF_UP);
        goal.setCurrentAmount(newAmount);
        goal.setUpdatedAt(java.time.Instant.now());

        SavingsGoal updated = repository.saveSavingsGoal(userId, goal);
        return new SavingsGoalResponse(updated);
    }

    public List<SavingsGoalResponse> getGoals(String userId) {
        List<SavingsGoal> goals = repository.findSavingsGoalsByUser(userId);
        return goals.stream()
                .map(SavingsGoalResponse::new)
                .collect(Collectors.toList());
    }

    public SavingsGoalResponse getGoalById(String userId, String goalId) {
        SavingsGoal goal = repository.findSavingsGoalById(userId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + goalId));
        return new SavingsGoalResponse(goal);
    }

    public void deleteGoal(String userId, String goalId) {
        boolean deleted = repository.deleteSavingsGoal(userId, goalId);
        if (!deleted) {
            throw new ResourceNotFoundException("Savings goal not found with id: " + goalId);
        }
    }

    private void validateSavingsGoalRequest(SavingsGoalRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Goal name is required.");
        }
        if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be a positive number.");
        }
        if (request.getCurrentAmount() != null && request.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Already saved amount cannot be negative.");
        }
    }
}
