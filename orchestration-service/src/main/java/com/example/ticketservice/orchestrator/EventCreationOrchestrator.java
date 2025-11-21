package com.example.ticketservice.orchestrator;

import com.example.ticketservice.client.EventServiceClient;
import com.example.ticketservice.client.NotificationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventCreationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(EventCreationOrchestrator.class);

    @Autowired
    private EventServiceClient eventClient;

    @Autowired
    private NotificationServiceClient notificationClient;

    public Map<String, Object> orchestrateEventCreation(String userEmail, Map<String, Object> eventData) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("INICIANDO ORQUESTACIÓN DE CREACIÓN DE EVENTO");
        log.info("Creado por: {}", userEmail);
        log.info("═══════════════════════════════════════════════════════════");

        try {
            // PASO 1: Crear evento en event-service
            log.info("PASO 1: Creando evento en event-service");
            Map<String, Object> eventResponse = eventClient.createEvento(eventData);
            String eventoNombre = (String) eventResponse.get("nombre");
            String fechaEvento = (String) eventResponse.get("fechaEvento");
            String ubicacion = (String) eventResponse.get("ubicacion");
            log.info("  ✓ Evento creado: {} - {}", eventoNombre, fechaEvento);

            // PASO 2: Enviar notificación de evento creado
            log.info("PASO 2: Enviando notificación de evento creado");
            sendEventCreatedNotification(userEmail, eventoNombre, fechaEvento, ubicacion);
            log.info("  ✓ Notificación enviada");

            log.info("═══════════════════════════════════════════════════════════");
            log.info("✓ ORQUESTACIÓN DE CREACIÓN DE EVENTO COMPLETADA");
            log.info("═══════════════════════════════════════════════════════════");

            return eventResponse;

        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ERROR EN ORQUESTACIÓN DE CREACIÓN DE EVENTO: {}", e.getMessage());
            log.error("═══════════════════════════════════════════════════════════");
            throw new RuntimeException("Error al procesar la creación del evento: " + e.getMessage(), e);
        }
    }

    private void sendEventCreatedNotification(String email, String eventoNombre, String fechaEvento, String ubicacion) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("tipo", "EVENTO_CREADO");
        notification.put("destinatario", email);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("eventoNombre", eventoNombre);
        datos.put("fechaEvento", fechaEvento);
        datos.put("ubicacion", ubicacion);
        
        notification.put("datos", datos);
        
        try {
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de evento creado: {}", e.getMessage());
        }
    }
}
