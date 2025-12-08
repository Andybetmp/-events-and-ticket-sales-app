package com.example.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("sendNotificationDelegate")
@RequiredArgsConstructor
public class SendNotificationDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Enviando notificación - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        String userEmail = (String) execution.getVariable("userEmail");
        String ticketId = (String) execution.getVariable("ticketId");
        String eventoNombre = (String) execution.getVariable("eventoNombre");
        Integer cantidad = ((Number) execution.getVariable("cantidad")).intValue();
        String tipoEntradaNombre = (String) execution.getVariable("tipoEntradaNombre");
        Double montoTotal = (Double) execution.getVariable("montoTotal");
        
        // Preparar datos de la notificación
        Map<String, Object> datos = new HashMap<>();
        datos.put("ticketId", ticketId);
        datos.put("eventoNombre", eventoNombre);
        datos.put("cantidad", cantidad);
        datos.put("tipoEntrada", tipoEntradaNombre);
        datos.put("total", montoTotal);
        
        Map<String, Object> notificationRequest = new HashMap<>();
        notificationRequest.put("tipo", "TICKET_COMPRADO");
        notificationRequest.put("destinatario", userEmail);
        notificationRequest.put("datos", datos);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(notificationRequest, headers);
        
        String url = notificationServiceUrl + "/api/notifications/send";
        restTemplate.postForEntity(url, request, Void.class);
        
        log.info("✅ Notificación enviada a: {}", userEmail);
    }
}
