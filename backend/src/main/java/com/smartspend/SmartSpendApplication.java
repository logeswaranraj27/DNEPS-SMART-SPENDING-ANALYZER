package com.smartspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartSpend: A Web-Based Personal Finance Management and
 * Spending Analytics System for College Students.
 * 
 * Main Spring Boot Application Entry Point.
 */
@SpringBootApplication
public class SmartSpendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSpendApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" SmartSpend Backend (Java 21 + Spring Boot + Firebase) is RUNNING ");
        System.out.println(" Local API URL: http://localhost:8080/api                         ");
        System.out.println(" Health check:  http://localhost:8080/api/health                  ");
        System.out.println("==================================================================");
    }
}
