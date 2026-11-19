package com.smartspend.service;

import com.smartspend.dto.TransactionRequest;
import com.smartspend.dto.TransactionResponse;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.Transaction;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 1 Service: Authoritative Expense and Income Management.
 * Implements strict monetary validation, itemized breakdown checks, and search filters.
 */
@Service
public class TransactionService {

    private final FirestoreRepository repository;

    public TransactionService(FirestoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new transaction with validated itemized details.
     */
    public TransactionResponse createTransaction(String userId, TransactionRequest request) {
        validateTransactionRequest(request);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType(request.getType().toLowerCase());
        tx.setCategory(request.getCategory().trim());
        tx.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        tx.setDate(request.getDate());
        tx.setDescription(request.getDescription());
        tx.setItems(request.getItems() != null ? request.getItems() : new ArrayList<>());

        Transaction saved = repository.saveTransaction(userId, tx);
        return new TransactionResponse(saved);
    }

    /**
     * Update an existing transaction.
     */
    public TransactionResponse updateTransaction(String userId, String transactionId, TransactionRequest request) {
        validateTransactionRequest(request);

        Transaction existing = repository.findTransactionById(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        existing.setType(request.getType().toLowerCase());
        existing.setCategory(request.getCategory().trim());
        existing.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        existing.setDate(request.getDate());
        existing.setDescription(request.getDescription());
        existing.setItems(request.getItems() != null ? request.getItems() : new ArrayList<>());
        existing.setUpdatedAt(java.time.Instant.now());

        Transaction updated = repository.saveTransaction(userId, existing);
        return new TransactionResponse(updated);
    }

    /**
     * List and filter transactions for the authenticated user.
     */
    public List<TransactionResponse> getTransactions(String userId, String type, String category,
                                                    LocalDate startDate, LocalDate endDate, String search) {
        List<Transaction> transactions = repository.findTransactionsByUser(userId);

        return transactions.stream()
                .filter(tx -> type == null || type.equalsIgnoreCase("all") || tx.getType().equalsIgnoreCase(type))
                .filter(tx -> category == null || category.equalsIgnoreCase("all") || tx.getCategory().equalsIgnoreCase(category))
                .filter(tx -> startDate == null || !tx.getDate().isBefore(startDate))
                .filter(tx -> endDate == null || !tx.getDate().isAfter(endDate))
                .filter(tx -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.trim().toLowerCase();
                    boolean matchCat = tx.getCategory() != null && tx.getCategory().toLowerCase().contains(q);
                    boolean matchDesc = tx.getDescription() != null && tx.getDescription().toLowerCase().contains(q);
                    boolean matchItems = tx.getItems() != null && tx.getItems().stream()
                            .anyMatch(i -> i.getName() != null && i.getName().toLowerCase().contains(q));
                    return matchCat || matchDesc || matchItems;
                })
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(String userId, String transactionId) {
        Transaction tx = repository.findTransactionById(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        return new TransactionResponse(tx);
    }

    public void deleteTransaction(String userId, String transactionId) {
        boolean deleted = repository.deleteTransaction(userId, transactionId);
        if (!deleted) {
            throw new ResourceNotFoundException("Transaction not found with id: " + transactionId);
        }
    }

    /**
     * Authoritative Validation Rules:
     * 1. Amount must be positive.
     * 2. Type must be 'income' or 'expense'.
     * 3. Item amounts must be valid positive values.
     * 4. Itemized total MUST equal the transaction amount!
     */
    public void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be a positive monetary value.");
        }

        String type = request.getType();
        if (type == null || (!type.equalsIgnoreCase("income") && !type.equalsIgnoreCase("expense"))) {
            throw new BadRequestException("Transaction type must be 'income' or 'expense'.");
        }

        if (request.getDate() == null) {
            throw new BadRequestException("Transaction date is required.");
        }

        // Validate itemized expenses if present
        if (type.equalsIgnoreCase("expense") && request.getItems() != null && !request.getItems().isEmpty()) {
            BigDecimal itemSum = BigDecimal.ZERO;
            for (TransactionItem item : request.getItems()) {
                if (item.getName() == null || item.getName().trim().isEmpty()) {
                    throw new BadRequestException("Purchased item name cannot be empty.");
                }
                if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException("Item '" + item.getName() + "' amount must be greater than zero.");
                }
                itemSum = itemSum.add(item.getAmount().setScale(2, RoundingMode.HALF_UP));
            }

            BigDecimal txAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
            if (itemSum.compareTo(txAmount) != 0) {
                throw new BadRequestException(String.format(
                        "Itemized total (₹%s) does not match transaction amount (₹%s). " +
                        "Items must add up exactly to the total expense.",
                        itemSum.toPlainString(), txAmount.toPlainString()
                ));
            }
        }
    }
}
