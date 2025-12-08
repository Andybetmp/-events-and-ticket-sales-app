package com.example.ticketservice.client;

import com.example.ticketservice.config.ServiceUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicación con Ticket-Service.
 * 
 * PATRÓN: REST Client - Encapsula las llamadas HTTP a otro microservicio.
 * 
 * RESPONSABILIDADES:
 * 1. Hacer peticiones HTTP POST/GET al ticket-service (puerto 8086)
 * 2. Añadir header X-Gateway-Secret para autenticación entre servicios
 * 3. Transformar excepciones HTTP en excepciones de negocio
 * 4. Serializar/deserializar JSON automáticamente
 * 
 * VENTAJAS:
 * - Orchestration-service NO necesita conocer el modelo Ticket ni acceder a su BD
 * - Separación clara de responsabilidades (Bounded Context)
 * - Cada servicio puede escalar independientemente
 * 
 * COMUNICACIÓN:
 * Orchestrator → este cliente → HTTP → Ticket-Service → BD (ticket_db)
 */
@Slf4j
@Component
public class TicketServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    /**
     * Crea un ticket en el ticket-service mediante una petición HTTP POST.
     * 
     * Este método NO guarda el ticket localmente, lo envía al servicio responsable.
     * El ticket-service es el dueño del dominio "Ticket" y su base de datos.
     * 
     * @return Map con los datos del ticket creado (ticketId, total, etc.)
     */
    public Map<String, Object> crearTicket(Long usuarioId, Long tipoEntradaId, String eventoNombre,
                                          String tipoEntradaNombre, Integer cantidad, 
                                          Double precioUnitario, String paymentId) {
        String url = serviceUrls.getTicketService().getUrl() + "/api/tickets";
        
        // Construir request body con todos los datos del ticket
        Map<String, Object> request = new HashMap<>();
        request.put("usuarioId", usuarioId);
        request.put("tipoEntradaId", tipoEntradaId);
        request.put("eventoNombre", eventoNombre);
        request.put("tipoEntradaNombre", tipoEntradaNombre);
        request.put("cantidad", cantidad);
        request.put("precioUnitario", precioUnitario);
        request.put("paymentId", paymentId);
        
        // Añadir header de autenticación entre servicios (X-Gateway-Secret)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            // Llamada HTTP POST al ticket-service
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            log.info("Ticket creado exitosamente");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al crear ticket: {}", e.getMessage());
            throw new RuntimeException("Error al crear ticket: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los tickets de un usuario mediante HTTP GET.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de tickets con toda su información
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> obtenerTicketsPorUsuario(Long usuarioId) {
        String url = serviceUrls.getTicketService().getUrl() + "/api/tickets/user/" + usuarioId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al obtener tickets del usuario {}: {}", usuarioId, e.getMessage());
            throw new RuntimeException("Error al obtener tickets: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una reserva temporal de entradas (decrementa stock por 10 minutos)
     */
    public Map<String, Object> crearReserva(Long tipoEntradaId, Long usuarioId, Integer cantidad) {
        String url = serviceUrls.getTicketService().getUrl() + "/api/reservas/crear";
        
        Map<String, Object> request = new HashMap<>();
        request.put("tipoEntradaId", tipoEntradaId);
        request.put("usuarioId", usuarioId);
        request.put("cantidad", cantidad);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            log.info("Reserva creada exitosamente: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al crear reserva: {}", e.getMessage());
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Confirma una reserva después de un pago exitoso
     */
    public Map<String, Object> confirmarReserva(Long reservaId) {
        String url = serviceUrls.getTicketService().getUrl() + "/api/reservas/" + reservaId + "/confirmar";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("Reserva confirmada exitosamente");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al confirmar reserva {}: {}", reservaId, e.getMessage());
            throw new RuntimeException("Error al confirmar reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Libera una reserva y restaura el stock
     */
    public Map<String, Object> liberarReserva(Long reservaId) {
        String url = serviceUrls.getTicketService().getUrl() + "/api/reservas/" + reservaId + "/liberar";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", gatewaySecret);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("Reserva liberada exitosamente");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al liberar reserva {}: {}", reservaId, e.getMessage());
            throw new RuntimeException("Error al liberar reserva: " + e.getMessage(), e);
        }
    }
}
