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
@Component("processPaymentDelegate")
@RequiredArgsConstructor
public class ProcessPaymentDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.payment-service.url}")
    private String paymentServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Procesando pago - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        // Obtener variables del proceso
        Double monto = (Double) execution.getVariable("monto");
        String cardNumber = (String) execution.getVariable("cardNumber");
        String cvv = (String) execution.getVariable("cvv");
        String expiryDate = (String) execution.getVariable("expiryDate");
        String cardHolder = (String) execution.getVariable("cardHolder");
        String idempotencyKey = (String) execution.getVariable("idempotencyKey");
        
        // Preparar request
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("idempotencyKey", idempotencyKey);
        paymentRequest.put("monto", monto);
        paymentRequest.put("cardNumber", cardNumber);
        paymentRequest.put("cvv", cvv);
        paymentRequest.put("expiryDate", expiryDate);
        paymentRequest.put("cardHolder", cardHolder);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentRequest, headers);
        
        // Llamar a payment-service
        String url = paymentServiceUrl + "/api/payments/authorize";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> paymentResponse = response.getBody();
        
        String paymentStatus = (String) paymentResponse.get("status");
        String paymentId = (String) paymentResponse.get("paymentId");
        String mensaje = (String) paymentResponse.get("mensaje");
        
        // Guardar resultado en variables del proceso
        execution.setVariable("paymentStatus", paymentStatus);
        execution.setVariable("paymentId", paymentId);
        execution.setVariable("paymentMessage", mensaje);
        
        log.info("✅ Pago procesado - Status: {} - PaymentID: {}", paymentStatus, paymentId);
    }
}
