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
@Component("createTicketDelegate")
@RequiredArgsConstructor
public class CreateTicketDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.ticket-service.url}")
    private String ticketServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Creando ticket - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        Long reservaId = ((Number) execution.getVariable("reservaId")).longValue();
        Long usuarioId = ((Number) execution.getVariable("usuarioId")).longValue();
        Long tipoEntradaId = ((Number) execution.getVariable("tipoEntradaId")).longValue();
        String paymentId = (String) execution.getVariable("paymentId");
        Double precio = (Double) execution.getVariable("precio");
        Integer cantidad = ((Number) execution.getVariable("cantidad")).intValue();
        String eventoNombre = (String) execution.getVariable("eventoNombre");
        String tipoEntradaNombre = (String) execution.getVariable("tipoEntradaNombre");
        
        Map<String, Object> ticketRequest = new HashMap<>();
        ticketRequest.put("reservaId", reservaId);
        ticketRequest.put("usuarioId", usuarioId);
        ticketRequest.put("tipoEntradaId", tipoEntradaId);
        ticketRequest.put("paymentId", paymentId);
        ticketRequest.put("precioUnitario", precio);
        ticketRequest.put("cantidad", cantidad);
        ticketRequest.put("eventoNombre", eventoNombre);
        ticketRequest.put("tipoEntradaNombre", tipoEntradaNombre);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(ticketRequest, headers);
        
        String url = ticketServiceUrl + "/api/tickets";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> ticketResponse = response.getBody();
        
        String ticketId = (String) ticketResponse.get("ticketId");
        
        execution.setVariable("ticketId", ticketId);
        
        log.info("✅ Ticket creado - TicketID: {}", ticketId);
    }
}
