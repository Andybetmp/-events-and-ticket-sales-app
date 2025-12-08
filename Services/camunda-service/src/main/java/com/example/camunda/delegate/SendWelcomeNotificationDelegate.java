package com.example.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component("sendWelcomeNotificationDelegate")
@RequiredArgsConstructor
public class SendWelcomeNotificationDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String userEmail = (String) execution.getVariable("userEmail");
        String userName = (String) execution.getVariable("userName");

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", userName);

        Map<String, Object> notificationRequest = new HashMap<>();
        notificationRequest.put("tipo", "BIENVENIDA");
        notificationRequest.put("destinatario", userEmail);
        notificationRequest.put("datos", datos);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Gateway-Secret", gatewaySecret);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(notificationRequest, headers);

        String url = notificationServiceUrl + "/api/notifications/send";
        restTemplate.postForEntity(url, request, String.class);

        execution.setVariable("welcomeNotificationSent", true);
    }
}
