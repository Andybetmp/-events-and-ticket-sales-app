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
@Component("createReservaDelegate")
@RequiredArgsConstructor
public class CreateReservaDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.ticket-service.url}")
    private String ticketServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Creando reserva temporal - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        Long tipoEntradaId = ((Number) execution.getVariable("tipoEntradaId")).longValue();
        Long usuarioId = ((Number) execution.getVariable("usuarioId")).longValue();
        Integer cantidad = ((Number) execution.getVariable("cantidad")).intValue();
        
        Map<String, Object> reservaRequest = new HashMap<>();
        reservaRequest.put("tipoEntradaId", tipoEntradaId);
        reservaRequest.put("usuarioId", usuarioId);
        reservaRequest.put("cantidad", cantidad);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reservaRequest, headers);
        
        String url = ticketServiceUrl + "/api/reservas/crear";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> reservaResponse = response.getBody();
        
        Long reservaId = ((Number) reservaResponse.get("id")).longValue();
        
        execution.setVariable("reservaId", reservaId);
        
        log.info("✅ Reserva creada - ReservaID: {}", reservaId);
    }
}
