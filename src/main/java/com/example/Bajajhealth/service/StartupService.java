// filepath: src/main/java/com/example/Bajajhealth/service/StartupService.java

package com.example.Bajajhealth.service;

import com.example.Bajajhealth.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StartupService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupService.class);
    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
    private static final int MAX_RETRIES = 4;
    private static final long RETRY_DELAY_MS = 1000;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("Application starting up - initiating webhook generation...");

        // Replace with your information
        String yourName = "Monil Lakhotia";
        String yourRegNo = "RA2211028010093"; // Use the RegNo provided for testing
        String yourEmail = "ml5858@srmist.edu.in";

        GenerateWebhookRequest requestPayload = new GenerateWebhookRequest(yourName, yourRegNo, yourEmail);
        GenerateWebhookResponse initialResponse = null;

        try {
            log.info("Sending POST request to: {}", GENERATE_WEBHOOK_URL);
            log.info("Request Body: Name={}, RegNo={}, Email={}", 
                    requestPayload.getName(), requestPayload.getRegNo(), requestPayload.getEmail());

            ResponseEntity<GenerateWebhookResponse> responseEntity = restTemplate.postForEntity(
                    GENERATE_WEBHOOK_URL,
                    requestPayload,
                    GenerateWebhookResponse.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                initialResponse = responseEntity.getBody();
                log.info("Successfully received webhook details.");
                log.info("Webhook URL: {}", initialResponse.getWebhook());
                log.info("Access Token: {}", initialResponse.getAccessToken());
                processResponseData(initialResponse, yourRegNo);
            } else {
                log.error("Failed to get valid response from generateWebhook. Status: {}", responseEntity.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error calling generateWebhook endpoint: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during startup processing: {}", e.getMessage(), e);
        }

        log.info("Startup processing finished.");
    }

    private void processResponseData(GenerateWebhookResponse response, String regNo) {
        GenerateWebhookResponseData responseData = response.getData();
        if (responseData == null || responseData.getUsers() == null) {
            log.error("Response data or users field is null. Cannot process.");
            return;
        }

        Object resultOutcome = null;
        List<User> userList = null;

        try {
            Object usersData = responseData.getUsers();
            if (usersData instanceof List) {
                log.debug("Detected List structure for users data.");
                userList = objectMapper.convertValue(usersData, new TypeReference<List<User>>() {});
            } else if (usersData instanceof Map) {
                log.debug("Detected Map structure for users data.");
                Map<String, Object> usersMap = objectMapper.convertValue(usersData, new TypeReference<Map<String, Object>>() {});
                Object nestedUsers = usersMap.get("users");
                if (nestedUsers instanceof List) {
                    userList = objectMapper.convertValue(nestedUsers, new TypeReference<List<User>>() {});
                } else {
                    log.error("Nested 'users' field within the map is not a List.");
                    return;
                }
            } else {
                log.error("Unexpected type for 'users' field in response data: {}", usersData.getClass().getName());
                return;
            }

            if (userList == null) {
                log.error("Could not extract user list from response.");
                return;
            }

            // Solve Question 1 regardless of regNo
            log.info("Solving Question 1: Mutual Followers.");
            resultOutcome = findMutualFollowers(userList);

            log.info("Successfully calculated outcome.");
            sendResultToWebhook(response.getWebhook(), response.getAccessToken(), regNo, resultOutcome);
        } catch (Exception e) {
            log.error("Error during data processing or problem solving: {}", e.getMessage(), e);
        }
    }

    private void sendResultToWebhook(String webhookUrl, String accessToken, String regNo, Object resultOutcome) {
        if (webhookUrl == null || accessToken == null || resultOutcome == null) {
            log.error("Missing webhookUrl, accessToken, or resultOutcome. Cannot send result.");
            return;
        }

        WebhookResult<List<List<Integer>>> resultPayload = new WebhookResult<>(regNo, (List<List<Integer>>) resultOutcome);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        try {
            log.debug("Webhook request body: {}", objectMapper.writeValueAsString(resultPayload));
        } catch (Exception e) {
            log.warn("Could not log request body: {}", e.getMessage());
        }

        HttpEntity<WebhookResult<List<List<Integer>>>> resultEntity = new HttpEntity<>(resultPayload, headers);
        boolean success = false;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Attempt {} to POST result to webhook: {}", attempt, webhookUrl);
                ResponseEntity<String> webhookResponseEntity = restTemplate.exchange(
                        webhookUrl,
                        HttpMethod.POST,
                        resultEntity,
                        String.class);

                if (webhookResponseEntity.getStatusCode().is2xxSuccessful()) {
                    log.info("Successfully posted result to webhook. Status: {}, Response: {}", 
                            webhookResponseEntity.getStatusCode(), webhookResponseEntity.getBody());
                    success = true;
                    break;
                } else {
                    log.warn("Webhook POST attempt {} failed with status: {}", 
                            attempt, webhookResponseEntity.getStatusCode());
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.warn("Webhook POST attempt {} failed with HTTP error: {} {} - Response Body: '{}'",
                        attempt, e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString(), e);
            } catch (RestClientException e) {
                log.warn("Webhook POST attempt {} failed with client error: {}", 
                        attempt, e.getMessage(), e);
            }

            if (!success && attempt < MAX_RETRIES) {
                try {
                    log.info("Waiting {}ms before retry...", RETRY_DELAY_MS);
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry delay interrupted.", ie);
                    break;
                }
            }
        }

        if (!success) {
            log.error("Failed to post result to webhook after {} attempts.", MAX_RETRIES);
        }
    }

    // Logic for Question 1: Mutual Followers
    private List<List<Integer>> findMutualFollowers(List<User> users) {
        Map<Integer, Set<Integer>> userFollowers = new HashMap<>();
        
        // Build a map of who follows whom
        for (User user : users) {
            userFollowers.put(user.getId(), new HashSet<>(user.getFollows()));
        }
        
        List<List<Integer>> mutuals = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();
        
        for (User user : users) {
            int id1 = user.getId();
            for (int id2 : user.getFollows()) {
                // Check if user2 also follows user1 (mutual following)
                if (userFollowers.containsKey(id2) && userFollowers.get(id2).contains(id1)) {
                    int minId = Math.min(id1, id2);
                    int maxId = Math.max(id1, id2);
                    String pairKey = minId + "-" + maxId;
                    
                    // Ensure we only add each pair once
                    if (!seenPairs.contains(pairKey)) {
                        mutuals.add(Arrays.asList(minId, maxId));
                        seenPairs.add(pairKey);
                    }
                }
            }
        }
        
        return mutuals;
    }
}
