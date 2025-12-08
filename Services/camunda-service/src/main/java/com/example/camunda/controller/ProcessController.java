package com.example.camunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/camunda")
@RequiredArgsConstructor
public class ProcessController {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RestTemplate restTemplate;

    @Value("${services.ticket-service.url}")
    private String ticketServiceUrl;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    // ========== REGISTRO DE USUARIO ==========
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> request) {
        try {
            log.info("🚀 Iniciando proceso de registro de usuario: {}", request.get("email"));
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombre", request.get("nombre"));
            variables.put("apellido", request.get("apellido"));
            variables.put("email", request.get("email"));
            variables.put("contrasena", request.get("contrasena"));
            
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "user-registration-process",
                variables
            );
            
            // Esperar hasta que termine
            int attempts = 0;
            while (attempts < 20) {
                ProcessInstance check = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstance.getId())
                    .singleResult();
                
                if (check == null) {
                    break;
                }
                
                Thread.sleep(300);
                attempts++;
            }
            
            // Obtener variables del historial
            List<HistoricVariableInstance> historicVariables = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .list();
            
            Map<String, Object> usuario = null;
            String token = null;
            
            for (HistoricVariableInstance var : historicVariables) {
                if ("usuario".equals(var.getName())) {
                    usuario = (Map<String, Object>) var.getValue();
                } else if ("token".equals(var.getName())) {
                    token = (String) var.getValue();
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("usuario", usuario);
            response.put("token", token);
            
            log.info("✅ Usuario registrado - Email: {}", request.get("email"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en registro: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== CREACIÓN DE EVENTO ==========
    @PostMapping("/create-event")
    public ResponseEntity<?> createEvent(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody Map<String, Object> request) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Header X-User-ID es requerido"));
        }
        
        try {
            log.info("🚀 Iniciando proceso de creación de evento: {}", request.get("nombre"));
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("usuarioId", userId);
            variables.put("userEmail", userEmail);
            variables.put("eventoNombre", request.get("nombre"));
            variables.put("eventoDescripcion", request.get("descripcion"));
            variables.put("ubicacion", request.get("ubicacion"));
            variables.put("fechaEvento", request.get("fechaEvento"));
            variables.put("categoria", request.get("categoria"));
            variables.put("imagenUrl", request.get("imagenUrl"));
            variables.put("tiposEntrada", request.get("tiposEntrada"));
            
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "event-creation-process",
                variables
            );
            
            // Esperar hasta que termine
            int attempts = 0;
            while (attempts < 20) {
                ProcessInstance check = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstance.getId())
                    .singleResult();
                
                if (check == null) {
                    break;
                }
                
                Thread.sleep(300);
                attempts++;
            }
            
            // Obtener del historial
            List<HistoricVariableInstance> historicVariables = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("evento")
                .singleResult() != null ?
                    historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .list() : null;
            
            Map<String, Object> evento = null;
            if (historicVariables != null) {
                for (HistoricVariableInstance var : historicVariables) {
                    if ("evento".equals(var.getName())) {
                        evento = (Map<String, Object>) var.getValue();
                        break;
                    }
                }
            }
            
            log.info("✅ Evento creado - Nombre: {}", request.get("nombre"));
            
            return ResponseEntity.ok(evento);
            
        } catch (Exception e) {
            log.error("❌ Error creando evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== COMPRA DE TICKET ==========

    @PostMapping("/purchase-ticket")
    public ResponseEntity<?> purchaseTicket(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody Map<String, Object> request) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().body("Header X-User-ID es requerido");
        }
        
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Header X-User-Email es requerido");
        }

        try {
            log.info("🚀 Iniciando proceso de compra de ticket para usuario: {}", userId);
            
            // Preparar variables del proceso
            Map<String, Object> variables = new HashMap<>();
            variables.put("usuarioId", userId);
            variables.put("userEmail", userEmail);
            variables.put("tipoEntradaId", request.get("tipoEntradaId"));
            variables.put("cantidad", request.get("cantidad"));
            variables.put("idempotencyKey", request.get("idempotencyKey"));
            
            // Datos de pago
            Map<String, Object> paymentMethod = (Map<String, Object>) request.get("paymentMethod");
            variables.put("cardNumber", paymentMethod.get("cardNumber"));
            variables.put("cvv", paymentMethod.get("cvv"));
            variables.put("expiryDate", paymentMethod.get("expiryDate"));
            variables.put("cardHolder", paymentMethod.get("cardHolder"));
            
            // Iniciar proceso de Camunda
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "ticket-purchase-process",
                variables
            );
            
            log.info("✅ Proceso iniciado - Process Instance ID: {}", processInstance.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("processInstanceId", processInstance.getId());
            response.put("message", "Proceso de compra iniciado");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error iniciando proceso: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/process-status/{processInstanceId}")
    public ResponseEntity<?> getProcessStatus(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        
        if (processInstance == null) {
            return ResponseEntity.ok(Map.of("status", "COMPLETED"));
        }
        
        return ResponseEntity.ok(Map.of("status", "RUNNING", "processInstanceId", processInstance.getId()));
    }

    // ========== CONSULTAR MIS TICKETS ==========
    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets(
            @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Header X-User-ID es requerido"));
        }
        
        try {
            log.info("📋 Consultando tickets del usuario: {}", userId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Gateway-Secret", gatewaySecret);
            headers.set("X-User-ID", userId.toString());
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = ticketServiceUrl + "/api/tickets/user/" + userId;
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            
            log.info("✅ Tickets obtenidos: {}", response.getBody().size());
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("❌ Error consultando tickets: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "camunda-service"));
    }
}
