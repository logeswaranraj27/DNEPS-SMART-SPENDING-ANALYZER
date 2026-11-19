package com.smartspend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * DTO for questions sent to DNEPS AI.
 */
public class AiQuestionRequest {

    @NotBlank(message = "Question cannot be empty")
    private String question;

    private Map<String, Object> context; // e.g. mode: "Easy Mode", "Balanced Mode", "Savings Focus"

    public AiQuestionRequest() {}

    public AiQuestionRequest(String question, Map<String, Object> context) {
        this.question = question;
        this.context = context;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
