package com.example.ticketservice.client;

import com.example.ticketservice.config.ServiceUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    @SuppressWarnings("unchecked")
    public Map<String, Object> registerUser(Map<String, Object> userData) {
        String url = serviceUrls.getUserService().getUrl() + "/api/users/register";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, userData, Map.class);
        return response.getBody();
    }
}
