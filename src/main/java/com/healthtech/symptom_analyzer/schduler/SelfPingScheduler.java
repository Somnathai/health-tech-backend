package com.healthtech.symptom_analyzer.schduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SelfPingScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedDelay = 300000) // every 10 minutes
    public void keepAlive() {
        try {
            restTemplate.getForObject(
                    "https://health-tech-api.onrender.com/actuator/health",
                    String.class
            );
            System.out.println("Keep-alive ping sent");
        } catch (Exception e) {
            System.out.println("Keep-alive ping failed: " + e.getMessage());
        }
    }
}