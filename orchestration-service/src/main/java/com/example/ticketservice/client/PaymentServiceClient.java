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
public class PaymentServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    public Map<String, Object> authorize(Map<String, Object> paymentRequest) {
        String url = serviceUrls.getPaymentService().getUrl() + "/api/payments/authorize";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentRequest, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        
        return response.getBody();
    }
}
