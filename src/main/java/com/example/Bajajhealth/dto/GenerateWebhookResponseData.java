// filepath: src/main/java/com/example/Bajajhealth/dto/GenerateWebhookResponseData.java

package com.example.Bajajhealth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateWebhookResponseData {
    // Change this field to Object to handle different JSON structures
    private Object users;

    // Getters and Setters for the Object field
    public Object getUsers() { return users; }
    public void setUsers(Object users) { this.users = users; }
}
