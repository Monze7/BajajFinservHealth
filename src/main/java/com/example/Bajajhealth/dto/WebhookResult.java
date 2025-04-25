// filepath: src/main/java/com/example/Bajajhealth/dto/WebhookResult.java

package com.example.Bajajhealth.dto;

// Use generics because the outcome is List<List<Integer>> for Q1
public class WebhookResult<T> {
    private String regNo;
    private T outcome;

    // Constructor needed for creating the payload
    public WebhookResult(String regNo, T outcome) {
        this.regNo = regNo;
        this.outcome = outcome;
    }

    // Getters are needed for JSON serialization
    public String getRegNo() { return regNo; }
    public T getOutcome() { return outcome; }

    // Setters might be needed by some frameworks, good practice to include
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public void setOutcome(T outcome) { this.outcome = outcome; }
}
