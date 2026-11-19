package com.smartspend.service;

import com.smartspend.dto.SavingsGoalRequest;
import com.smartspend.dto.SavingsGoalResponse;
import com.smartspend.exception.BadRequestException;
import com.smartspend.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 3: Savings Goals & Rate Calculations")
class SavingsGoalServiceTest {

    private FirestoreRepository repository;
    private SavingsGoalService service;

    @BeforeEach
    void setUp() {
        repository = new FirestoreRepository();
        service = new SavingsGoalService(repository);
    }

    @Test
    @DisplayName("Should accurately calculate remaining amount, progress percentage, and suggested weekly rate (Target ₹4000, Saved ₹1000)")
    void testSavingsGoalCalculations() {
        // Target: ₹4,000, Saved: ₹1,000, Target date in 28 days (4 weeks)
        LocalDate targetDate = LocalDate.now().plusDays(28);
        SavingsGoalRequest req = new SavingsGoalRequest(
                "Headphones", "Noise cancelling", BigDecimal.valueOf(4000), BigDecimal.valueOf(1000), targetDate
        );

        SavingsGoalResponse resp = service.createGoal("user1", req);
        assertEquals(0, BigDecimal.valueOf(3000).compareTo(resp.getRemainingAmount())); // 4000 - 1000
        assertEquals(25.0, resp.getProgressPercentage()); // (1000 / 4000) * 100
        assertEquals("In Progress", resp.getStatus());
        assertEquals(0, BigDecimal.valueOf(750).compareTo(resp.getSuggestedWeeklySavingsRate())); // 3000 / 4
    }

    @Test
    @DisplayName("Should update progress when adding a contribution deposit")
    void testContributionDeposit() {
        LocalDate targetDate = LocalDate.now().plusDays(30);
        SavingsGoalRequest req = new SavingsGoalRequest(
                "Headphones", "Audio", BigDecimal.valueOf(4000), BigDecimal.valueOf(1000), targetDate
        );

        SavingsGoalResponse created = service.createGoal("user1", req);

        // Add ₹500 contribution
        SavingsGoalResponse updated = service.contribute("user1", created.getId(), BigDecimal.valueOf(500));
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(updated.getCurrentAmount()));
        assertEquals(0, BigDecimal.valueOf(2500).compareTo(updated.getRemainingAmount()));
        assertEquals(37.5, updated.getProgressPercentage());
    }

    @Test
    @DisplayName("Should handle fully funded and completed goals")
    void testFullyFundedGoal() {
        SavingsGoalRequest req = new SavingsGoalRequest(
                "Small Gear", "Adapter", BigDecimal.valueOf(500), BigDecimal.valueOf(500), LocalDate.now().plusDays(10)
        );

        SavingsGoalResponse resp = service.createGoal("user1", req);
        assertEquals("Fully Funded", resp.getStatus());
        assertEquals(100.0, resp.getProgressPercentage());
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.getRemainingAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.getSuggestedWeeklySavingsRate()));
    }

    @Test
    @DisplayName("Should reject invalid or zero target amounts")
    void testRejectInvalidTargetAmount() {
        SavingsGoalRequest zeroReq = new SavingsGoalRequest(
                "Invalid", "Test", BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(10)
        );
        assertThrows(BadRequestException.class, () -> service.createGoal("user1", zeroReq));
    }
}
