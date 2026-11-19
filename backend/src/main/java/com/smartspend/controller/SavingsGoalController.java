package com.smartspend.controller;

import com.smartspend.dto.ContributionRequest;
import com.smartspend.dto.SavingsGoalRequest;
import com.smartspend.dto.SavingsGoalResponse;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Module 3: Savings Goals Management.
 */
@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        String uid = SecurityUtils.getAuthenticatedUid();
        SavingsGoalResponse response = savingsGoalService.createGoal(uid, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getGoals() {
        String uid = SecurityUtils.getAuthenticatedUid();
        List<SavingsGoalResponse> list = savingsGoalService.getGoals(uid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        SavingsGoalResponse response = savingsGoalService.getGoalById(uid, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable String id,
            @Valid @RequestBody SavingsGoalRequest request) {

        String uid = SecurityUtils.getAuthenticatedUid();
        SavingsGoalResponse response = savingsGoalService.updateGoal(uid, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalResponse> contribute(
            @PathVariable String id,
            @Valid @RequestBody ContributionRequest request) {

        String uid = SecurityUtils.getAuthenticatedUid();
        SavingsGoalResponse response = savingsGoalService.contribute(uid, id, request.getAmount());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        savingsGoalService.deleteGoal(uid, id);
        return ResponseEntity.noContent().build();
    }
}
