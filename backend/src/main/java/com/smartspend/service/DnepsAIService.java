package com.smartspend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.dto.AiQuestionRequest;
import com.smartspend.dto.AiQuestionResponse;
import com.smartspend.dto.WeeklyReviewResponse;
import com.smartspend.model.Budget;
import com.smartspend.model.SavingsGoal;
import com.smartspend.model.Transaction;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 3 Service: DNEPS AI ("Your Personal Smart Spending & Savings Buddy").
 * Computes exact authoritative financial statistics in Java and interfaces
 * securely with the server-side language model API (Gemini / OpenAI / Demo).
 */
@Service
public class DnepsAIService {

    private static final Logger logger = LoggerFactory.getLogger(DnepsAIService.class);

    private final FirestoreRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.provider:GEMINI}")
    private String aiProvider;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model:gemini-3.5-flash-lite}")
    private String modelName;

    @Value("${ai.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    public DnepsAIService(FirestoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Ask DNEPS AI a conversational financial question.
     */
    public AiQuestionResponse ask(String userId, AiQuestionRequest request) {
        String question = request.getQuestion().trim();
        String mode = "Balanced Mode";
        if (request.getContext() != null && request.getContext().containsKey("mode")) {
            mode = request.getContext().get("mode").toString();
        }

        // 1. Authoritative financial analysis computed strictly in Java
        JavaFinancialContext ctx = buildAuthoritativeFinancialContext(userId);

        // 2. If API Key is configured, call live LLM
        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("your_gemini_api_key")) {
            try {
                String aiResponseText = callLiveLanguageModel(question, mode, ctx);
                AiQuestionResponse resp = new AiQuestionResponse(aiResponseText, false, aiProvider);
                resp.setSuggestedFollowUps(generateFollowUps(question, ctx));
                return resp;
            } catch (Exception e) {
                logger.error("Error communicating with AI API: {}", e.getMessage());
                // Fall back gracefully to Java-computed answer
            }
        }

        // 3. Graceful fallback demonstration mode based on the exact Java calculations
        String demoAnswer = generateIntelligentDemoResponse(question, mode, ctx);
        AiQuestionResponse resp = new AiQuestionResponse(demoAnswer, true, "DEMO");
        resp.setSuggestedFollowUps(generateFollowUps(question, ctx));
        return resp;
    }

    /**
     * Section 7.F: Authoritative 7-Day Weekly Spending Review.
     */
    public WeeklyReviewResponse getWeeklySpendingReview(String userId) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate fourteenDaysAgo = today.minusDays(14);

        // Current 7 days expenses
        List<Transaction> currentWeekTx = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && !tx.getDate().isBefore(sevenDaysAgo) && !tx.getDate().isAfter(today))
                .collect(Collectors.toList());

        BigDecimal currentWeekTotal = currentWeekTx.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Previous 7 days expenses
        List<Transaction> previousWeekTx = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && tx.getDate().isBefore(sevenDaysAgo) && !tx.getDate().isBefore(fourteenDaysAgo))
                .collect(Collectors.toList());

        BigDecimal previousWeekTotal = previousWeekTx.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top category current week
        Map<String, BigDecimal> currentCategories = new HashMap<>();
        for (Transaction tx : currentWeekTx) {
            String cat = tx.getCategory() != null ? tx.getCategory() : "Other expenses";
            currentCategories.put(cat, currentCategories.getOrDefault(cat, BigDecimal.ZERO).add(tx.getAmount()));
        }

        String topCat = "None";
        BigDecimal topCatAmount = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : currentCategories.entrySet()) {
            if (e.getValue().compareTo(topCatAmount) > 0) {
                topCatAmount = e.getValue();
                topCat = e.getKey();
            }
        }

        // Highest cost item recorded in current week
        String highestItem = "None";
        BigDecimal highestItemAmount = BigDecimal.ZERO;
        for (Transaction tx : currentWeekTx) {
            if (tx.getItems() != null && !tx.getItems().isEmpty()) {
                for (TransactionItem item : tx.getItems()) {
                    if (item.getAmount() != null && item.getAmount().compareTo(highestItemAmount) > 0) {
                        highestItemAmount = item.getAmount();
                        highestItem = item.getName();
                    }
                }
            } else if (tx.getAmount().compareTo(highestItemAmount) > 0) {
                highestItemAmount = tx.getAmount();
                highestItem = tx.getDescription() != null && !tx.getDescription().isEmpty() 
                        ? tx.getDescription() : tx.getCategory();
            }
        }

        // Categories where spending increased vs previous week
        Map<String, BigDecimal> prevCategories = new HashMap<>();
        for (Transaction tx : previousWeekTx) {
            String cat = tx.getCategory() != null ? tx.getCategory() : "Other expenses";
            prevCategories.put(cat, prevCategories.getOrDefault(cat, BigDecimal.ZERO).add(tx.getAmount()));
        }

        List<String> increased = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : currentCategories.entrySet()) {
            BigDecimal prev = prevCategories.getOrDefault(e.getKey(), BigDecimal.ZERO);
            if (e.getValue().compareTo(prev) > 0) {
                increased.add(e.getKey() + " (+₹" + e.getValue().subtract(prev) + ")");
            }
        }

        WeeklyReviewResponse review = new WeeklyReviewResponse();
        review.setTotalWeeklyExpense(currentWeekTotal);
        review.setPreviousWeekExpense(previousWeekTotal);
        review.setDifference(currentWeekTotal.subtract(previousWeekTotal));
        review.setTopCategory(topCat);
        review.setTopCategoryAmount(topCatAmount);
        review.setHighestCostItem(highestItem);
        review.setHighestItemAmount(highestItemAmount);
        review.setIncreasedCategories(increased);

        // Generate tailored insights
        List<String> insights = new ArrayList<>();
        if (currentWeekTotal.compareTo(BigDecimal.ZERO) == 0) {
            insights.add("No expenses recorded over the last 7 days. Start logging daily expenses to generate insights.");
        } else {
            if (currentWeekTotal.compareTo(previousWeekTotal) > 0) {
                insights.add(String.format("Weekly spending increased by ₹%s compared to the prior 7 days.", currentWeekTotal.subtract(previousWeekTotal)));
            } else if (previousWeekTotal.compareTo(BigDecimal.ZERO) > 0) {
                insights.add(String.format("Great job! You spent ₹%s less than the previous week.", previousWeekTotal.subtract(currentWeekTotal)));
            }

            if (!"None".equals(topCat)) {
                insights.add(String.format("Your largest spending category this week was %s (₹%s).", topCat, topCatAmount));
            }

            if (!"None".equals(highestItem)) {
                insights.add(String.format("Your single highest recorded item was '%s' at ₹%s.", highestItem, highestItemAmount));
            }
        }

        review.setInsights(insights);
        // Suggested next week budget = current week spend minus 15% optimization target
        BigDecimal suggestedPlan = currentWeekTotal.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);
        review.setSuggestedNextWeekBudget(suggestedPlan.compareTo(BigDecimal.ZERO) > 0 ? suggestedPlan : BigDecimal.valueOf(1000));

        return review;
    }

    /**
     * Call Live Google Gemini Language Model API securely from Java backend.
     */
    private String callLiveLanguageModel(String userQuestion, String mode, JavaFinancialContext ctx) throws Exception {
        String endpoint = String.format("%s/%s:generateContent?key=%s", geminiApiUrl, modelName, apiKey);

        String systemPrompt = String.format(
                "You are DNEPS AI, 'Your Personal Smart Spending & Savings Buddy', a friendly, nonjudgmental financial assistant for college students.\n" +
                "PERSONALIZED SPENDING MODE: %s\n" +
                "AUTHORITATIVE FINANCIAL FACTS (Calculated strictly by Java Backend, do not invent or recalculate):\n" +
                "- Current Month Total Income: ₹%s\n" +
                "- Current Month Total Expenses: ₹%s\n" +
                "- Current Month Net Balance: ₹%s\n" +
                "- Last 7 Days Spending: ₹%s (Previous 7 Days: ₹%s)\n" +
                "- Top Spending Category: %s (₹%s)\n" +
                "- Highest Single Expense: %s (₹%s)\n" +
                "- Snack & Canteen Purchases (7 Days): ₹%s across items: %s\n" +
                "- Active Savings Goals: %s\n" +
                "- Budgets Over Limit: %s\n\n" +
                "RULES:\n" +
                "1. Answer the student's question accurately using ONLY the authoritative facts above.\n" +
                "2. Keep advice respectful, supportive, practical for a college student, and concise.\n" +
                "3. Never invent transactions, prices, or bank account balances.",
                mode,
                ctx.monthlyIncome, ctx.monthlyExpense, ctx.netAmount,
                ctx.weeklyExpense, ctx.previousWeeklyExpense,
                ctx.topCategory, ctx.topCategoryAmount,
                ctx.highestExpenseDesc, ctx.highestExpenseAmount,
                ctx.snackSpending, String.join(", ", ctx.snackItems),
                ctx.savingsGoalsSummary,
                ctx.exceededBudgets
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt + "\n\nStudent Question: " + userQuestion)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "maxOutputTokens", 400
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        }

        throw new RuntimeException("Unexpected format returned from Gemini API");
    }

    /**
     * Generates an intelligent, accurate demonstration response using the
     * exact authoritative Java calculations when no API key is configured.
     */
    private String generateIntelligentDemoResponse(String q, String mode, JavaFinancialContext ctx) {
        String lower = q.toLowerCase();

        if (lower.contains("snack") || lower.contains("food") || lower.contains("canteen")) {
            return String.format(
                    "Based on your recorded transactions for the last 7 days, you spent **₹%s** on canteen snacks and drinks%s. " +
                    "Under **%s**, reducing these purchases by 25%% could help you save approximately **₹%s** towards your savings goals!",
                    ctx.snackSpending,
                    ctx.snackItems.isEmpty() ? "" : " (including " + String.join(", ", ctx.snackItems) + ")",
                    mode,
                    ctx.snackSpending.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP)
            );
        } else if (lower.contains("biggest") || lower.contains("highest") || lower.contains("largest")) {
            return String.format(
                    "Your biggest recorded expense was **%s** for **₹%s** in the **%s** category.",
                    ctx.highestExpenseDesc, ctx.highestExpenseAmount, ctx.topCategory
            );
        } else if (lower.contains("month") || lower.contains("summar")) {
            return String.format(
                    "Here is your monthly summary: Total income is **₹%s**, total expenses are **₹%s**, " +
                    "giving you a net period balance of **₹%s**. Your top spending area is **%s** (₹%s).",
                    ctx.monthlyIncome, ctx.monthlyExpense, ctx.netAmount, ctx.topCategory, ctx.topCategoryAmount
            );
        } else if (lower.contains("week") || lower.contains("review")) {
            String comparison = ctx.weeklyExpense.compareTo(ctx.previousWeeklyExpense) > 0
                    ? String.format("which is **₹%s more** than the prior week", ctx.weeklyExpense.subtract(ctx.previousWeeklyExpense))
                    : "which shows disciplined spending compared to the prior period";
            return String.format(
                    "Over the last 7 days, your total spending was **₹%s**, %s. " +
                    "Top category: **%s** (₹%s). Suggested target for next week: **₹%s**.",
                    ctx.weeklyExpense, comparison, ctx.topCategory, ctx.topCategoryAmount,
                    ctx.weeklyExpense.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP)
            );
        } else if (lower.contains("budget") || lower.contains("exceed")) {
            if ("None".equals(ctx.exceededBudgets)) {
                return "Good news! All your active budgets are currently within safe limits.";
            } else {
                return String.format("Notice: You have exceeded budget limits in: %s. Consider slowing spending in these categories.", ctx.exceededBudgets);
            }
        } else if (lower.contains("goal") || lower.contains("save") || lower.contains("rate")) {
            return String.format(
                    "Your active savings goals: %s. Under **%s**, setting aside 15%% of your weekly allowance (₹%s) will help you stay on track!",
                    ctx.savingsGoalsSummary, mode,
                    ctx.monthlyIncome.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP)
            );
        }

        return String.format(
                "Hello! I've reviewed your records: This month you have spent **₹%s** with total income of **₹%s** (Net: **₹%s**). " +
                "Your top category is **%s**. What would you like to explore (snacks, budget status, or weekly review)?",
                ctx.monthlyExpense, ctx.monthlyIncome, ctx.netAmount, ctx.topCategory
        );
    }

    private List<String> generateFollowUps(String question, JavaFinancialContext ctx) {
        return List.of(
                "How much did I spend on snacks this week?",
                "Which category did I spend the most on?",
                "Give me my weekly spending review",
                "Where could I reduce my spending?"
        );
    }

    /**
     * Compute authoritative Java financial snapshot.
     */
    private JavaFinancialContext buildAuthoritativeFinancialContext(String userId) {
        JavaFinancialContext ctx = new JavaFinancialContext();
        List<Transaction> transactions = repository.findTransactionsByUser(userId);
        List<Budget> budgets = repository.findBudgetsByUser(userId, null);
        List<SavingsGoal> goals = repository.findSavingsGoalsByUser(userId);

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate fourteenDaysAgo = today.minusDays(14);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String currentMonth = today.format(monthFormatter);

        // Monthly Totals
        ctx.monthlyIncome = transactions.stream()
                .filter(tx -> "income".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && tx.getDate().format(monthFormatter).equals(currentMonth))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        ctx.monthlyExpense = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && tx.getDate().format(monthFormatter).equals(currentMonth))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        ctx.netAmount = ctx.monthlyIncome.subtract(ctx.monthlyExpense);

        // 7 Days Spending
        ctx.weeklyExpense = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && !tx.getDate().isBefore(sevenDaysAgo))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        ctx.previousWeeklyExpense = transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .filter(tx -> tx.getDate() != null && tx.getDate().isBefore(sevenDaysAgo) && !tx.getDate().isBefore(fourteenDaysAgo))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top category & highest expense
        Map<String, BigDecimal> catMap = new HashMap<>();
        BigDecimal maxTxAmount = BigDecimal.ZERO;
        String maxTxDesc = "None";

        for (Transaction tx : transactions) {
            if ("expense".equalsIgnoreCase(tx.getType())) {
                String cat = tx.getCategory() != null ? tx.getCategory() : "Other";
                catMap.put(cat, catMap.getOrDefault(cat, BigDecimal.ZERO).add(tx.getAmount()));

                if (tx.getAmount().compareTo(maxTxAmount) > 0) {
                    maxTxAmount = tx.getAmount();
                    maxTxDesc = (tx.getDescription() != null && !tx.getDescription().isEmpty()) ? tx.getDescription() : tx.getCategory();
                }

                // Snack item scan
                if (tx.getDate() != null && !tx.getDate().isBefore(sevenDaysAgo) && tx.getItems() != null) {
                    for (TransactionItem item : tx.getItems()) {
                        String name = item.getName() != null ? item.getName().toLowerCase() : "";
                        if (name.contains("snack") || name.contains("chip") || name.contains("drink") ||
                            name.contains("tea") || name.contains("coffee") || name.contains("samosa") || 
                            name.contains("chocolate") || name.contains("cookie") || name.contains("biscuit") ||
                            name.contains("sweet") || name.contains("cake") || name.contains("juice") ||
                            name.contains("maggi") || name.contains("canteen") || "food".equalsIgnoreCase(tx.getCategory())) {
                            ctx.snackSpending = ctx.snackSpending.add(item.getAmount());
                            ctx.snackItems.add(item.getName() + " (₹" + item.getAmount() + ")");
                        }
                    }
                }
            }
        }

        ctx.highestExpenseDesc = maxTxDesc;
        ctx.highestExpenseAmount = maxTxAmount;

        String topC = "None";
        BigDecimal topA = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : catMap.entrySet()) {
            if (e.getValue().compareTo(topA) > 0) {
                topA = e.getValue();
                topC = e.getKey();
            }
        }
        ctx.topCategory = topC;
        ctx.topCategoryAmount = topA;

        // Goals summary
        if (goals.isEmpty()) {
            ctx.savingsGoalsSummary = "None created yet";
        } else {
            ctx.savingsGoalsSummary = goals.stream()
                    .map(g -> g.getName() + " (Saved: ₹" + g.getCurrentAmount() + " / ₹" + g.getTargetAmount() + ")")
                    .collect(Collectors.joining("; "));
        }

        // Exceeded budgets
        List<String> exceeded = new ArrayList<>();
        for (Budget b : budgets) {
            BigDecimal spent = transactions.stream()
                    .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                    .filter(tx -> tx.getDate() != null && tx.getDate().format(monthFormatter).equals(b.getMonth()))
                    .filter(tx -> "Overall Budget".equalsIgnoreCase(b.getCategory()) || (tx.getCategory() != null && tx.getCategory().equalsIgnoreCase(b.getCategory())))
                    .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (spent.compareTo(b.getLimitAmount()) > 0) {
                exceeded.add(b.getCategory() + " (Spent ₹" + spent + " / Limit ₹" + b.getLimitAmount() + ")");
            }
        }
        ctx.exceededBudgets = exceeded.isEmpty() ? "None" : String.join(", ", exceeded);

        return ctx;
    }

    private static class JavaFinancialContext {
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;
        BigDecimal netAmount = BigDecimal.ZERO;
        BigDecimal weeklyExpense = BigDecimal.ZERO;
        BigDecimal previousWeeklyExpense = BigDecimal.ZERO;
        String topCategory = "None";
        BigDecimal topCategoryAmount = BigDecimal.ZERO;
        String highestExpenseDesc = "None";
        BigDecimal highestExpenseAmount = BigDecimal.ZERO;
        BigDecimal snackSpending = BigDecimal.ZERO;
        List<String> snackItems = new ArrayList<>();
        String savingsGoalsSummary = "None";
        String exceededBudgets = "None";
    }
}
