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

@Component("registerUserDelegate")
@RequiredArgsConstructor
public class RegisterUserDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Obtener datos del proceso
        String nombre = (String) execution.getVariable("nombre");
        String apellido = (String) execution.getVariable("apellido");
        String email = (String) execution.getVariable("email");
        String telefono = (String) execution.getVariable("telefono");
        String contrasena = (String) execution.getVariable("contrasena");

        // Preparar request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", nombre);
        requestBody.put("apellido", apellido);
        requestBody.put("email", email);
        requestBody.put("telefono", telefono);
        requestBody.put("contrasena", contrasena);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Gateway-Secret", gatewaySecret);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Llamar a user-service
        String url = userServiceUrl + "/api/users/register";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> usuario = (Map<String, Object>) responseBody.get("usuario");

        // Guardar resultado en el proceso
        execution.setVariable("userId", usuario.get("id"));
        execution.setVariable("userEmail", usuario.get("email"));
        execution.setVariable("userName", usuario.get("nombre"));
        execution.setVariable("userApellido", usuario.get("apellido"));
        execution.setVariable("token", responseBody.get("token"));
        execution.setVariable("usuario", usuario);
    }
}
