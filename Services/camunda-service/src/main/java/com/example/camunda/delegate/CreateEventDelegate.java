package com.example.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("createEventDelegate")
@RequiredArgsConstructor
public class CreateEventDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;

    @Value("${services.event-service.url}")
    private String eventServiceUrl;
    
    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Obtener datos del evento
        String nombre = (String) execution.getVariable("eventoNombre");
        String descripcion = (String) execution.getVariable("eventoDescripcion");
        String ubicacion = (String) execution.getVariable("ubicacion");
        String fechaEvento = (String) execution.getVariable("fechaEvento");
        String categoria = (String) execution.getVariable("categoria");
        String imagenUrl = (String) execution.getVariable("imagenUrl");
        List<Map<String, Object>> tiposEntrada = (List<Map<String, Object>>) execution.getVariable("tiposEntrada");
        Long usuarioIdLong = (Long) execution.getVariable("usuarioId");
        String userEmail = (String) execution.getVariable("userEmail");
        Integer usuarioId = usuarioIdLong.intValue();

        // Obtener información del usuario para organizador (usar endpoint /email/ que está permitido)
        String userUrl = userServiceUrl + "/api/users/email/" + userEmail;
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("X-Gateway-Secret", gatewaySecret);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        
        ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, Map.class);
        Map<String, Object> usuario = userResponse.getBody();
        String organizador = usuario.get("nombre") + " " + usuario.get("apellido");

        // Preparar request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", nombre);
        requestBody.put("descripcion", descripcion);
        requestBody.put("ubicacion", ubicacion);
        requestBody.put("fechaEvento", fechaEvento);
        requestBody.put("categoria", categoria);
        requestBody.put("tiposEntrada", tiposEntrada);
        requestBody.put("organizadorId", usuarioIdLong);
        requestBody.put("organizador", organizador);
        
        // Agregar imagenUrl solo si existe
        if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
            requestBody.put("imagenUrl", imagenUrl);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Gateway-Secret", gatewaySecret);
        headers.set("X-User-ID", usuarioId.toString());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Llamar a event-service
        String url = eventServiceUrl + "/api/eventos";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> evento = response.getBody();

        // Guardar resultado
        execution.setVariable("eventoId", evento.get("id"));
        execution.setVariable("evento", evento);
        execution.setVariable("eventoCreated", true);
    }
}
