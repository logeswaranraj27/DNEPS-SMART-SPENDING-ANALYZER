package com.smartspend.controller;

import com.smartspend.dto.BudgetRequest;
import com.smartspend.dto.BudgetResponse;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Module 2: Budget Planning and Limits.
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        String uid = SecurityUtils.getAuthenticatedUid();
        BudgetResponse response = budgetService.createBudget(uid, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(@RequestParam(required = false) String month) {
        String uid = SecurityUtils.getAuthenticatedUid();
        List<BudgetResponse> list = budgetService.getBudgets(uid, month);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        BudgetResponse response = budgetService.getBudgetById(uid, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable String id,
            @Valid @RequestBody BudgetRequest request) {

        String uid = SecurityUtils.getAuthenticatedUid();
        BudgetResponse response = budgetService.updateBudget(uid, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        budgetService.deleteBudget(uid, id);
        return ResponseEntity.noContent().build();
    }
}
