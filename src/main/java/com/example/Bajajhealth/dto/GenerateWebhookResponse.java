// filepath: src/main/java/com/example/Bajajhealth/dto/GenerateWebhookResponse.java

package com.example.Bajajhealth.dto;

public class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;
    private GenerateWebhookResponseData data;

    // Getters and Setters
    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public GenerateWebhookResponseData getData() { return data; }
    public void setData(GenerateWebhookResponseData data) { this.data = data; }
}
