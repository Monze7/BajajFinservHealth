// filepath: src/main/java/com/example/Bajajhealth/dto/GenerateWebhookRequest.java

package com.example.Bajajhealth.dto;

public class GenerateWebhookRequest {
    private String name;
    private String regNo;
    private String email;

    // Default constructor (required for JSON deserialization)
    public GenerateWebhookRequest() {}

    public GenerateWebhookRequest(String name, String regNo, String email) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    // Getters and Setters (essential for JSON serialization)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
