package com.smartspend.controller;

import com.smartspend.dto.*;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Module 2: Spending Analytics & Avoidable Expense Analysis.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) String month) {

        String uid = SecurityUtils.getAuthenticatedUid();
        FinancialSummaryResponse summary = analyticsService.getFinancialSummary(uid, period, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/categories")
    public ResponseEntity<CategoryAnalyticsResponse> getCategoryAnalytics(
            @RequestParam(required = false) String month) {

        String uid = SecurityUtils.getAuthenticatedUid();
        CategoryAnalyticsResponse analytics = analyticsService.getCategoryAnalytics(uid, month);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/trends")
    public ResponseEntity<SpendingTrendsResponse> getSpendingTrends(
            @RequestParam(defaultValue = "14") int days) {

        String uid = SecurityUtils.getAuthenticatedUid();
        SpendingTrendsResponse trends = analyticsService.getSpendingTrends(uid, days);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/monthly-comparison")
    public ResponseEntity<MonthlyComparisonResponse> getMonthlyComparison(
            @RequestParam(required = false) Integer year) {

        String uid = SecurityUtils.getAuthenticatedUid();
        MonthlyComparisonResponse comparison = analyticsService.getMonthlyComparison(uid, year);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/where-you-could-save")
    public ResponseEntity<AvoidableSavingsResponse> getAvoidableSavings(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "30") int reductionPercent) {

        String uid = SecurityUtils.getAuthenticatedUid();
        AvoidableSavingsResponse savings = analyticsService.getAvoidableSavingsOpportunities(uid, month, reductionPercent);
        return ResponseEntity.ok(savings);
    }
}
