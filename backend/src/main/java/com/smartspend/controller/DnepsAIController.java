package com.smartspend.controller;

import com.smartspend.dto.AiQuestionRequest;
import com.smartspend.dto.AiQuestionResponse;
import com.smartspend.dto.WeeklyReviewResponse;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.DnepsAIService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Module 3: DNEPS AI ("Your Personal Smart Spending & Savings Buddy").
 */
@RestController
@RequestMapping("/api/ai")
public class DnepsAIController {

    private final DnepsAIService dnepsAIService;

    public DnepsAIController(DnepsAIService dnepsAIService) {
        this.dnepsAIService = dnepsAIService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiQuestionResponse> askQuestion(@Valid @RequestBody AiQuestionRequest request) {
        String uid = SecurityUtils.getAuthenticatedUid();
        AiQuestionResponse response = dnepsAIService.ask(uid, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly-review")
    public ResponseEntity<WeeklyReviewResponse> getWeeklyReview() {
        String uid = SecurityUtils.getAuthenticatedUid();
        WeeklyReviewResponse review = dnepsAIService.getWeeklySpendingReview(uid);
        return ResponseEntity.ok(review);
    }
}
