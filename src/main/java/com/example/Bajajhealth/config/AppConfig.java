// filepath: src/main/java/com/example/Bajajhealth/config/AppConfig.java

package com.example.Bajajhealth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        // Creates a RestTemplate instance managed by Spring
        return new RestTemplate();
    }
}
