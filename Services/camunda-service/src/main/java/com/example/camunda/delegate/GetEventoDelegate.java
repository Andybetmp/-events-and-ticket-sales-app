package com.example.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component("getEventoDelegate")
@RequiredArgsConstructor
public class GetEventoDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.event-service.url}")
    private String eventServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Obteniendo evento - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        Long eventoId = ((Number) execution.getVariable("eventoId")).longValue();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        String url = eventServiceUrl + "/api/eventos/" + eventoId;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<String, Object> evento = response.getBody();
        
        String eventoNombre = (String) evento.get("nombre");
        String descripcion = (String) evento.get("descripcion");
        String estadoEvento = (String) evento.get("estado");
        
        // Validar que el evento esté ACTIVO
        if (!"ACTIVO".equals(estadoEvento)) {
            String mensaje = "El evento no está disponible para compra. Estado actual: " + estadoEvento;
            log.error("❌ {}", mensaje);
            throw new RuntimeException(mensaje);
        }
        
        execution.setVariable("eventoNombre", eventoNombre);
        execution.setVariable("eventoDescripcion", descripcion);
        execution.setVariable("estadoEvento", estadoEvento);
        
        log.info("✅ Evento obtenido - Nombre: {} - Estado: {}", eventoNombre, estadoEvento);
    }
}
