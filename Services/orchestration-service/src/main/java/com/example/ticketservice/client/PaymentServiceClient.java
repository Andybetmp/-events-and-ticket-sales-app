package com.example.ticketservice.client;

import com.example.ticketservice.config.ServiceUrlsConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    @SuppressWarnings("unchecked")
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "paymentService", fallbackMethod = "authorizeFallback")
    public Map<String, Object> authorize(Map<String, Object> paymentRequest) {
        log.info("Attempting payment authorization (Circuit Breaker: paymentService)");
        String url = serviceUrls.getPaymentService().getUrl() + "/api/payments/authorize";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentRequest, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // El payment-service retorna 402 para pagos rechazados
            // Extraer el body con el detalle del rechazo
            try {
                Map<String, Object> errorBody = e.getResponseBodyAs(Map.class);
                return errorBody;
            } catch (Exception parseError) {
                // Si no se puede parsear el body, crear respuesta genérica
                Map<String, Object> rejectedResponse = new HashMap<>();
                rejectedResponse.put("status", "REJECTED");
                rejectedResponse.put("mensaje", "Pago rechazado: " + e.getMessage());
                return rejectedResponse;
            }
        }
    }
    
    // Fallback method for Circuit Breaker
    private Map<String, Object> authorizeFallback(Map<String, Object> paymentRequest, Exception ex) {
        log.error("Payment service unavailable, using fallback. Error: {}", ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "SERVICE_UNAVAILABLE");
        fallbackResponse.put("mensaje", "Servicio de pagos no disponible, intente más tarde");
        return fallbackResponse;
    }
}
