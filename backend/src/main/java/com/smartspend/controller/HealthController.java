package com.smartspend.controller;

import com.smartspend.repository.FirestoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public health and diagnostics endpoint for SmartSpend.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final FirestoreRepository repository;

    public HealthController(FirestoreRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "system", "SmartSpend Personal Finance Management System",
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "firestoreConnected", repository.isFirestoreConnected(),
                "timestamp", Instant.now().toString()
        ));
    }
}
