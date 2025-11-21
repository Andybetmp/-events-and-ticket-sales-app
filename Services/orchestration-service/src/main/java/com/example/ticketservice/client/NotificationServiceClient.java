package com.example.ticketservice.client;

import com.example.ticketservice.config.ServiceUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NotificationServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    public void sendNotification(Map<String, Object> notificationRequest) {
        String url = serviceUrls.getNotificationService().getUrl() + "/api/notifications/send";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(notificationRequest, headers);
        restTemplate.postForEntity(url, request, Map.class);
    }
}
