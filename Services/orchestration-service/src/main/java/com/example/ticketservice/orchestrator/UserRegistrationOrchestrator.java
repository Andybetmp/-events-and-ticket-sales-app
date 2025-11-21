package com.example.ticketservice.orchestrator;

import com.example.ticketservice.client.NotificationServiceClient;
import com.example.ticketservice.client.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserRegistrationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationOrchestrator.class);

    @Autowired
    private UserServiceClient userClient;

    @Autowired
    private NotificationServiceClient notificationClient;

    public Map<String, Object> orchestrateRegistration(Map<String, Object> userData) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("INICIANDO ORQUESTACIÓN DE REGISTRO DE USUARIO");
        log.info("Email: {}", userData.get("email"));
        log.info("═══════════════════════════════════════════════════════════");

        try {
            // PASO 1: Registrar usuario en user-service
            log.info("PASO 1: Registrando usuario en user-service");
            Map<String, Object> userResponse = userClient.registerUser(userData);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> usuario = (Map<String, Object>) userResponse.get("usuario");
            String nombre = (String) usuario.get("nombre");
            String email = (String) usuario.get("email");
            log.info("  ✓ Usuario registrado: {} ({})", nombre, email);

            // PASO 2: Enviar notificación de bienvenida
            log.info("PASO 2: Enviando notificación de bienvenida");
            sendWelcomeNotification(email, nombre);
            log.info("  ✓ Notificación enviada");

            log.info("═══════════════════════════════════════════════════════════");
            log.info("✓ ORQUESTACIÓN DE REGISTRO COMPLETADA");
            log.info("═══════════════════════════════════════════════════════════");

            return userResponse;

        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ERROR EN ORQUESTACIÓN DE REGISTRO: {}", e.getMessage());
            log.error("═══════════════════════════════════════════════════════════");
            throw new RuntimeException("Error al procesar el registro: " + e.getMessage(), e);
        }
    }

    private void sendWelcomeNotification(String email, String nombre) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("tipo", "BIENVENIDA");
        notification.put("destinatario", email);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        
        notification.put("datos", datos);
        
        try {
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de bienvenida: {}", e.getMessage());
        }
    }
}
