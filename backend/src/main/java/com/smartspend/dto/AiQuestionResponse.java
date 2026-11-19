package com.smartspend.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for responses from DNEPS AI ("Your Personal Smart Spending & Savings Buddy").
 */
public class AiQuestionResponse {
    private String answer;
    private boolean isDemo;
    private String provider; // "GEMINI", "OPENAI", or "DEMO"
    private List<String> suggestedFollowUps = new ArrayList<>();

    public AiQuestionResponse() {}

    public AiQuestionResponse(String answer, boolean isDemo, String provider) {
        this.answer = answer;
        this.isDemo = isDemo;
        this.provider = provider;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { isDemo = demo; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public List<String> getSuggestedFollowUps() { return suggestedFollowUps; }
    public void setSuggestedFollowUps(List<String> suggestedFollowUps) { this.suggestedFollowUps = suggestedFollowUps; }
}
