package com.smartspend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.smartspend.model.Budget;
import com.smartspend.model.SavingsGoal;
import com.smartspend.model.Transaction;
import com.smartspend.model.TransactionItem;
import com.smartspend.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data access repository for SmartSpend.
 * Implements strict user isolation (`users/{userId}/...`).
 * Operates on Google Cloud Firestore when connected, with thread-safe
 * fallback storage when cloud credentials have not yet been configured.
 * Uses explicit Map conversion to prevent LocalDate/BigDecimal reflection
 * serialization issues in Firestore SDK.
 */
@Repository
public class FirestoreRepository {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreRepository.class);

    @Autowired(required = false)
    private Firestore firestore;

    // Fallback store: Map<userId, Map<id, Entity>>
    private final Map<String, Map<String, Transaction>> transactionsFallback = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Budget>> budgetsFallback = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SavingsGoal>> savingsGoalsFallback = new ConcurrentHashMap<>();
    private final Map<String, UserProfile> userProfilesFallback = new ConcurrentHashMap<>();

    public boolean isFirestoreConnected() {
        return firestore != null;
    }

    // ==========================================
    // TRANSACTIONS (users/{userId}/transactions)
    // ==========================================

    public Transaction saveTransaction(String userId, Transaction transaction) {
        if (transaction.getId() == null || transaction.getId().trim().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        transaction.setUserId(userId);

        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transaction.getId());
                docRef.set(transactionToMap(transaction)).get();
                return transaction;
            } catch (Exception e) {
                logger.error("Error writing transaction to Firestore, falling back: {}", e.getMessage());
            }
        }

        transactionsFallback.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(transaction.getId(), transaction);
        return transaction;
    }

    public List<Transaction> findTransactionsByUser(String userId) {
        if (firestore != null) {
            try {
                CollectionReference col = firestore.collection("users")
                        .document(userId)
                        .collection("transactions");
                ApiFuture<QuerySnapshot> future = col.get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                return docs.stream().map(this::mapToTransaction).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Error reading transactions from Firestore, falling back: {}", e.getMessage());
            }
        }

        Map<String, Transaction> userMap = transactionsFallback.get(userId);
        if (userMap == null) return new ArrayList<>();
        return new ArrayList<>(userMap.values());
    }

    public Optional<Transaction> findTransactionById(String userId, String transactionId) {
        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transactionId)
                        .get().get();
                if (doc.exists()) {
                    return Optional.ofNullable(mapToTransaction(doc));
                }
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error reading single transaction from Firestore: {}", e.getMessage());
            }
        }

        Map<String, Transaction> userMap = transactionsFallback.get(userId);
        if (userMap != null && userMap.containsKey(transactionId)) {
            return Optional.of(userMap.get(transactionId));
        }
        return Optional.empty();
    }

    public boolean deleteTransaction(String userId, String transactionId) {
        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transactionId)
                        .delete().get();
                return true;
            } catch (Exception e) {
                logger.error("Error deleting transaction from Firestore: {}", e.getMessage());
            }
        }

        Map<String, Transaction> userMap = transactionsFallback.get(userId);
        if (userMap != null) {
            return userMap.remove(transactionId) != null;
        }
        return false;
    }

    // ==========================================
    // BUDGETS (users/{userId}/budgets)
    // ==========================================

    public Budget saveBudget(String userId, Budget budget) {
        if (budget.getId() == null || budget.getId().trim().isEmpty()) {
            budget.setId(UUID.randomUUID().toString());
        }
        budget.setUserId(userId);

        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(userId)
                        .collection("budgets")
                        .document(budget.getId())
                        .set(budgetToMap(budget)).get();
                return budget;
            } catch (Exception e) {
                logger.error("Error writing budget to Firestore, falling back: {}", e.getMessage());
            }
        }

        budgetsFallback.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(budget.getId(), budget);
        return budget;
    }

    public List<Budget> findBudgetsByUser(String userId, String month) {
        if (firestore != null) {
            try {
                Query query = firestore.collection("users")
                        .document(userId)
                        .collection("budgets");
                if (month != null && !month.trim().isEmpty()) {
                    query = query.whereEqualTo("month", month);
                }
                List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
                return docs.stream().map(this::mapToBudget).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Error reading budgets from Firestore, falling back: {}", e.getMessage());
            }
        }

        Map<String, Budget> userMap = budgetsFallback.get(userId);
        if (userMap == null) return new ArrayList<>();
        return userMap.values().stream()
                .filter(b -> month == null || month.trim().isEmpty() || month.equals(b.getMonth()))
                .collect(Collectors.toList());
    }

    public Optional<Budget> findBudgetById(String userId, String budgetId) {
        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users")
                        .document(userId)
                        .collection("budgets")
                        .document(budgetId)
                        .get().get();
                if (doc.exists()) {
                    return Optional.ofNullable(mapToBudget(doc));
                }
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error reading single budget from Firestore: {}", e.getMessage());
            }
        }

        Map<String, Budget> userMap = budgetsFallback.get(userId);
        if (userMap != null && userMap.containsKey(budgetId)) {
            return Optional.of(userMap.get(budgetId));
        }
        return Optional.empty();
    }

    public boolean deleteBudget(String userId, String budgetId) {
        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(userId)
                        .collection("budgets")
                        .document(budgetId)
                        .delete().get();
                return true;
            } catch (Exception e) {
                logger.error("Error deleting budget from Firestore: {}", e.getMessage());
            }
        }

        Map<String, Budget> userMap = budgetsFallback.get(userId);
        if (userMap != null) {
            return userMap.remove(budgetId) != null;
        }
        return false;
    }

    // ==========================================
    // SAVINGS GOALS (users/{userId}/savingsGoals)
    // ==========================================

    public SavingsGoal saveSavingsGoal(String userId, SavingsGoal goal) {
        if (goal.getId() == null || goal.getId().trim().isEmpty()) {
            goal.setId(UUID.randomUUID().toString());
        }
        goal.setUserId(userId);

        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(userId)
                        .collection("savingsGoals")
                        .document(goal.getId())
                        .set(savingsGoalToMap(goal)).get();
                return goal;
            } catch (Exception e) {
                logger.error("Error writing goal to Firestore, falling back: {}", e.getMessage());
            }
        }

        savingsGoalsFallback.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(goal.getId(), goal);
        return goal;
    }

    public List<SavingsGoal> findSavingsGoalsByUser(String userId) {
        if (firestore != null) {
            try {
                List<QueryDocumentSnapshot> docs = firestore.collection("users")
                        .document(userId)
                        .collection("savingsGoals")
                        .get().get().getDocuments();
                return docs.stream().map(this::mapToSavingsGoal).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Error reading goals from Firestore, falling back: {}", e.getMessage());
            }
        }

        Map<String, SavingsGoal> userMap = savingsGoalsFallback.get(userId);
        if (userMap == null) return new ArrayList<>();
        return new ArrayList<>(userMap.values());
    }

    public Optional<SavingsGoal> findSavingsGoalById(String userId, String goalId) {
        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users")
                        .document(userId)
                        .collection("savingsGoals")
                        .document(goalId)
                        .get().get();
                if (doc.exists()) {
                    return Optional.ofNullable(mapToSavingsGoal(doc));
                }
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error reading single goal from Firestore: {}", e.getMessage());
            }
        }

        Map<String, SavingsGoal> userMap = savingsGoalsFallback.get(userId);
        if (userMap != null && userMap.containsKey(goalId)) {
            return Optional.of(userMap.get(goalId));
        }
        return Optional.empty();
    }

    public boolean deleteSavingsGoal(String userId, String goalId) {
        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(userId)
                        .collection("savingsGoals")
                        .document(goalId)
                        .delete().get();
                return true;
            } catch (Exception e) {
                logger.error("Error deleting goal from Firestore: {}", e.getMessage());
            }
        }

        Map<String, SavingsGoal> userMap = savingsGoalsFallback.get(userId);
        if (userMap != null) {
            return userMap.remove(goalId) != null;
        }
        return false;
    }

    // ==========================================
    // USER PROFILE & PREFERENCES
    // ==========================================

    public UserProfile saveUserProfile(UserProfile profile) {
        if (firestore != null) {
            try {
                firestore.collection("users")
                        .document(profile.getUserId())
                        .set(userProfileToMap(profile)).get();
                return profile;
            } catch (Exception e) {
                logger.error("Error writing user profile to Firestore: {}", e.getMessage());
            }
        }

        userProfilesFallback.put(profile.getUserId(), profile);
        return profile;
    }

    public Optional<UserProfile> findUserProfile(String userId) {
        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users")
                        .document(userId)
                        .get().get();
                if (doc.exists()) {
                    return Optional.ofNullable(mapToUserProfile(doc));
                }
            } catch (Exception e) {
                logger.error("Error reading user profile from Firestore: {}", e.getMessage());
            }
        }

        return Optional.ofNullable(userProfilesFallback.get(userId));
    }

    // ==========================================
    // FIRESTORE MAP CONVERSION HELPERS
    // ==========================================

    private Map<String, Object> transactionToMap(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("userId", t.getUserId());
        map.put("type", t.getType());
        map.put("category", t.getCategory());
        map.put("amount", t.getAmount() != null ? t.getAmount().doubleValue() : 0.0);
        map.put("date", t.getDate() != null ? t.getDate().toString() : LocalDate.now().toString());
        map.put("description", t.getDescription());
        if (t.getItems() != null) {
            List<Map<String, Object>> itemMaps = new ArrayList<>();
            for (TransactionItem item : t.getItems()) {
                Map<String, Object> im = new HashMap<>();
                im.put("name", item.getName());
                im.put("amount", item.getAmount() != null ? item.getAmount().doubleValue() : 0.0);
                itemMaps.add(im);
            }
            map.put("items", itemMaps);
        }
        map.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : Instant.now().toString());
        map.put("updatedAt", t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : Instant.now().toString());
        return map;
    }

    @SuppressWarnings("unchecked")
    private Transaction mapToTransaction(DocumentSnapshot doc) {
        if (!doc.exists()) return null;
        Transaction t = new Transaction();
        t.setId(doc.getString("id") != null ? doc.getString("id") : doc.getId());
        t.setUserId(doc.getString("userId"));
        t.setType(doc.getString("type"));
        t.setCategory(doc.getString("category"));
        Double amount = doc.getDouble("amount");
        t.setAmount(amount != null ? BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        String dateStr = doc.getString("date");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                t.setDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                t.setDate(LocalDate.now());
            }
        } else {
            t.setDate(LocalDate.now());
        }
        t.setDescription(doc.getString("description"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
        if (items != null) {
            List<TransactionItem> list = new ArrayList<>();
            for (Map<String, Object> im : items) {
                String name = (String) im.get("name");
                Object amtObj = im.get("amount");
                BigDecimal amt = BigDecimal.ZERO;
                if (amtObj instanceof Number) {
                    amt = BigDecimal.valueOf(((Number) amtObj).doubleValue()).setScale(2, RoundingMode.HALF_UP);
                }
                list.add(new TransactionItem(name, amt));
            }
            t.setItems(list);
        }
        return t;
    }

    private Map<String, Object> savingsGoalToMap(SavingsGoal g) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", g.getId());
        map.put("userId", g.getUserId());
        map.put("name", g.getName());
        map.put("description", g.getDescription());
        map.put("targetAmount", g.getTargetAmount() != null ? g.getTargetAmount().doubleValue() : 0.0);
        map.put("currentAmount", g.getCurrentAmount() != null ? g.getCurrentAmount().doubleValue() : 0.0);
        map.put("targetDate", g.getTargetDate() != null ? g.getTargetDate().toString() : null);
        map.put("createdAt", g.getCreatedAt() != null ? g.getCreatedAt().toString() : Instant.now().toString());
        map.put("updatedAt", g.getUpdatedAt() != null ? g.getUpdatedAt().toString() : Instant.now().toString());
        return map;
    }

    private SavingsGoal mapToSavingsGoal(DocumentSnapshot doc) {
        if (!doc.exists()) return null;
        SavingsGoal g = new SavingsGoal();
        g.setId(doc.getString("id") != null ? doc.getString("id") : doc.getId());
        g.setUserId(doc.getString("userId"));
        g.setName(doc.getString("name"));
        g.setDescription(doc.getString("description"));
        Double target = doc.getDouble("targetAmount");
        g.setTargetAmount(target != null ? BigDecimal.valueOf(target).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        Double current = doc.getDouble("currentAmount");
        g.setCurrentAmount(current != null ? BigDecimal.valueOf(current).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        String targetDateStr = doc.getString("targetDate");
        if (targetDateStr != null && !targetDateStr.isEmpty()) {
            try {
                g.setTargetDate(LocalDate.parse(targetDateStr));
            } catch (Exception e) {
                g.setTargetDate(LocalDate.now().plusMonths(1));
            }
        }
        return g;
    }

    private Map<String, Object> budgetToMap(Budget b) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", b.getId());
        map.put("userId", b.getUserId());
        map.put("category", b.getCategory());
        map.put("limitAmount", b.getLimitAmount() != null ? b.getLimitAmount().doubleValue() : 0.0);
        map.put("month", b.getMonth());
        map.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : Instant.now().toString());
        map.put("updatedAt", b.getUpdatedAt() != null ? b.getUpdatedAt().toString() : Instant.now().toString());
        return map;
    }

    private Budget mapToBudget(DocumentSnapshot doc) {
        if (!doc.exists()) return null;
        Budget b = new Budget();
        b.setId(doc.getString("id") != null ? doc.getString("id") : doc.getId());
        b.setUserId(doc.getString("userId"));
        b.setCategory(doc.getString("category"));
        Double limit = doc.getDouble("limitAmount");
        b.setLimitAmount(limit != null ? BigDecimal.valueOf(limit).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        b.setMonth(doc.getString("month"));
        return b;
    }

    private Map<String, Object> userProfileToMap(UserProfile p) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", p.getUserId());
        map.put("email", p.getEmail());
        map.put("displayName", p.getDisplayName());
        map.put("collegeName", p.getCollegeName());
        map.put("spendingMode", p.getSpendingMode());
        map.put("currency", p.getCurrency());
        map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : Instant.now().toString());
        map.put("updatedAt", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : Instant.now().toString());
        return map;
    }

    private UserProfile mapToUserProfile(DocumentSnapshot doc) {
        if (!doc.exists()) return null;
        UserProfile p = new UserProfile();
        p.setUserId(doc.getString("userId") != null ? doc.getString("userId") : doc.getId());
        p.setEmail(doc.getString("email"));
        p.setDisplayName(doc.getString("displayName"));
        p.setCollegeName(doc.getString("collegeName"));
        if (doc.getString("spendingMode") != null) p.setSpendingMode(doc.getString("spendingMode"));
        if (doc.getString("currency") != null) p.setCurrency(doc.getString("currency"));
        return p;
    }
}
