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
@Component("getTipoEntradaDelegate")
@RequiredArgsConstructor
public class GetTipoEntradaDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    
    @Value("${services.event-service.url}")
    private String eventServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("🔹 Obteniendo tipo de entrada - ProcessInstanceID: {}", execution.getProcessInstanceId());
        
        Long tipoEntradaId = ((Number) execution.getVariable("tipoEntradaId")).longValue();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        String url = eventServiceUrl + "/api/tipos-entrada/" + tipoEntradaId;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<String, Object> tipoEntrada = response.getBody();
        
        Double precio = ((Number) tipoEntrada.get("precio")).doubleValue();
        String tipoEntradaNombre = (String) tipoEntrada.get("nombre");
        Long eventoId = ((Number) tipoEntrada.get("eventoId")).longValue();
        Integer cantidad = ((Number) execution.getVariable("cantidad")).intValue();
        
        Double montoTotal = precio * cantidad;
        
        execution.setVariable("precio", precio);
        execution.setVariable("tipoEntradaNombre", tipoEntradaNombre);
        execution.setVariable("eventoId", eventoId);
        execution.setVariable("monto", montoTotal);
        
        log.info("✅ Tipo de entrada obtenido - Precio: {} - Monto Total: {}", precio, montoTotal);
    }
}
