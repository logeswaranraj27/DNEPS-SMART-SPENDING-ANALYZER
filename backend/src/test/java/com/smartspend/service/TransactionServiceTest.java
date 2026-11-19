package com.smartspend.service;

import com.smartspend.dto.TransactionRequest;
import com.smartspend.dto.TransactionResponse;
import com.smartspend.exception.BadRequestException;
import com.smartspend.model.TransactionItem;
import com.smartspend.repository.FirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1: Transaction & Itemized Expense Tests")
class TransactionServiceTest {

    private FirestoreRepository repository;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        repository = new FirestoreRepository();
        service = new TransactionService(repository);
    }

    @Test
    @DisplayName("Should successfully record single expense without itemization")
    void testCreateSingleExpense() {
        TransactionRequest req = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(150), LocalDate.now(), "Quick lunch", null
        );

        TransactionResponse resp = service.createTransaction("user1", req);
        assertNotNull(resp.getId());
        assertEquals("expense", resp.getType());
        assertEquals("Food", resp.getCategory());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(resp.getAmount()));
    }

    @Test
    @DisplayName("Should successfully record valid itemized expense (Lunch ₹150 + Chips ₹60 + Drinks ₹70 + Chocolate ₹70 = ₹350)")
    void testCreateItemizedExpenseSuccess() {
        List<TransactionItem> items = List.of(
                new TransactionItem("Lunch", BigDecimal.valueOf(150)),
                new TransactionItem("Chips", BigDecimal.valueOf(60)),
                new TransactionItem("Cool drink", BigDecimal.valueOf(70)),
                new TransactionItem("Chocolate", BigDecimal.valueOf(70))
        );

        TransactionRequest req = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(350), LocalDate.now(), "Canteen items", items
        );

        TransactionResponse resp = service.createTransaction("user1", req);
        assertNotNull(resp.getId());
        assertEquals(4, resp.getItems().size());
        assertEquals(0, BigDecimal.valueOf(350).compareTo(resp.getAmount()));
    }

    @Test
    @DisplayName("Should reject itemized expense when item sum does not equal transaction amount")
    void testRejectItemizedExpenseMismatch() {
        List<TransactionItem> items = List.of(
                new TransactionItem("Lunch", BigDecimal.valueOf(150)),
                new TransactionItem("Chips", BigDecimal.valueOf(60))
                // Sum is 210, but total is 350
        );

        TransactionRequest req = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(350), LocalDate.now(), "Mismatch total", items
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createTransaction("user1", req));
        assertTrue(ex.getMessage().contains("does not match transaction amount"));
    }

    @Test
    @DisplayName("Should reject negative or zero transaction amounts")
    void testRejectInvalidAmounts() {
        TransactionRequest zeroReq = new TransactionRequest(
                "expense", "Food", BigDecimal.ZERO, LocalDate.now(), "Zero amount", null
        );
        assertThrows(BadRequestException.class, () -> service.createTransaction("user1", zeroReq));

        TransactionRequest negativeReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(-50), LocalDate.now(), "Negative amount", null
        );
        assertThrows(BadRequestException.class, () -> service.createTransaction("user1", negativeReq));
    }

    @Test
    @DisplayName("Should reject item with empty name or negative item amount")
    void testRejectInvalidItemRows() {
        List<TransactionItem> items = List.of(
                new TransactionItem("", BigDecimal.valueOf(100))
        );
        TransactionRequest emptyNameReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(100), LocalDate.now(), "Empty item", items
        );
        assertThrows(BadRequestException.class, () -> service.createTransaction("user1", emptyNameReq));

        List<TransactionItem> negativeItems = List.of(
                new TransactionItem("Invalid", BigDecimal.valueOf(-100))
        );
        TransactionRequest negativeItemReq = new TransactionRequest(
                "expense", "Food", BigDecimal.valueOf(100), LocalDate.now(), "Negative item", negativeItems
        );
        assertThrows(BadRequestException.class, () -> service.createTransaction("user1", negativeItemReq));
    }

    @Test
    @DisplayName("Should search expenses by individual item name")
    void testSearchByItemName() {
        List<TransactionItem> items = List.of(
                new TransactionItem("Algorithm Textbook", BigDecimal.valueOf(500))
        );
        TransactionRequest req = new TransactionRequest(
                "expense", "Education", BigDecimal.valueOf(500), LocalDate.now(), "College study materials", items
        );
        service.createTransaction("user1", req);

        List<TransactionResponse> results = service.getTransactions("user1", null, null, null, null, "Textbook");
        assertEquals(1, results.size());
        assertEquals("Education", results.get(0).getCategory());
    }
}
