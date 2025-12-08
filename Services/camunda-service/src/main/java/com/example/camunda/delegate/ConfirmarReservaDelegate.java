package com.example.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component("confirmarReservaDelegate")
@RequiredArgsConstructor
public class ConfirmarReservaDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.ticket-service.url}")
    private String ticketServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Confirmando reserva - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        Long reservaId = ((Number) execution.getVariable("reservaId")).longValue();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        String url = ticketServiceUrl + "/api/reservas/" + reservaId + "/confirmar";
        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        
        log.info("✅ Reserva confirmada - ReservaID: {}", reservaId);
    }
}
