package com.smartspend.controller;

import com.smartspend.dto.TransactionRequest;
import com.smartspend.dto.TransactionResponse;
import com.smartspend.security.SecurityUtils;
import com.smartspend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Module 1: Income and Expense Management.
 * All operations enforce user isolation using verified Firebase token UID.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        String uid = SecurityUtils.getAuthenticatedUid();
        TransactionResponse response = transactionService.createTransaction(uid, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {

        String uid = SecurityUtils.getAuthenticatedUid();
        List<TransactionResponse> list = transactionService.getTransactions(uid, type, category, startDate, endDate, search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        TransactionResponse response = transactionService.getTransactionById(uid, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {

        String uid = SecurityUtils.getAuthenticatedUid();
        TransactionResponse response = transactionService.updateTransaction(uid, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        String uid = SecurityUtils.getAuthenticatedUid();
        transactionService.deleteTransaction(uid, id);
        return ResponseEntity.noContent().build();
    }
}
