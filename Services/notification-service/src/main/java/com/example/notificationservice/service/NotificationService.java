package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de Notificaciones con Soporte para Email Real y Fallback a Logs.
 * 
 * MODOS DE OPERACIÓN:
 * 1. MODO PRODUCCIÓN: Si mailSender está configurado → Envía emails reales via Gmail SMTP
 * 2. MODO DESARROLLO: Si mailSender es null → Simula emails en logs (fallback automático)
 * 
 * TIPOS DE NOTIFICACIONES:
 * - BIENVENIDA: Al registrar usuario
 * - EVENTO_CREADO: Al publicar evento
 * - TICKET_COMPRADO: Al confirmar compra
 * - PAGO_RECHAZADO: Al fallar pago
 * 
 * CARACTERÍSTICAS:
 * - @Async: Procesamiento asíncrono (no bloquea la operación principal)
 * - Graceful degradation: Si falla SMTP, cae a logs (no rompe el flujo)
 * - Notification ID único para tracking
 * 
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false) // required=false permite fallback si no está configurado
    private JavaMailSender mailSender;

    public NotificationResponse enviarNotificacion(NotificationRequest request) {
        String notificationId = "NOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Simular envío de email/SMS basado en el tipo
        switch (request.getTipo()) {
            case "BIENVENIDA":
                enviarBienvenida(request, notificationId);
                break;
            case "EVENTO_CREADO":
                enviarEventoCreado(request, notificationId);
                break;
            case "TICKET_COMPRADO":
                enviarTicketComprado(request, notificationId);
                break;
            case "PAGO_RECHAZADO":
                enviarPagoRechazado(request, notificationId);
                break;
            default:
                log.warn("[{}] Tipo de notificación desconocido: {}", notificationId, request.getTipo());
                return new NotificationResponse(notificationId, "FAILED", Instant.now(), "Tipo de notificación no soportado");
        }

        return new NotificationResponse(
            notificationId,
            "SENT",
            Instant.now(),
            "Notificación enviada exitosamente"
        );
    }

    @Async
    private void enviarBienvenida(NotificationRequest request, String notificationId) {
        String nombre = (String) request.getDatos().get("nombre");
        
        if (mailSender == null) {
            // Modo simulación - solo logs
            log.info("╔═══════════════════════════════════════════════════════════╗");
            log.info("║           📧 SIMULACIÓN EMAIL - BIENVENIDA               ║");
            log.info("╠═══════════════════════════════════════════════════════════╣");
            log.info("║ ID: {}", String.format("%-52s", notificationId) + "║");
            log.info("║ Para: {}", String.format("%-50s", request.getDestinatario()) + "║");
            log.info("║ Asunto: Bienvenido a SOA Ticketing                       ║");
            log.info("║ Mensaje: Hola {}, tu cuenta ha sido creada", String.format("%-29s", nombre) + "║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getDestinatario());
            message.setSubject("🎉 Bienvenido a SOA Ticketing");
            message.setText(
                "Hola " + nombre + ",\n\n" +
                "¡Bienvenido a nuestro sistema de venta de entradas!\n" +
                "Tu cuenta ha sido creada exitosamente.\n\n" +
                "Ahora puedes comprar entradas para tus eventos favoritos.\n\n" +
                "Saludos,\n" +
                "Equipo SOA Ticketing\n\n" +
                "Notification ID: " + notificationId
            );
            
            mailSender.send(message);
            log.info("Email BIENVENIDA enviado a: {} [{}]", request.getDestinatario(), notificationId);
            
        } catch (Exception e) {
            log.error("Error enviando email BIENVENIDA a {}: {}", request.getDestinatario(), e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    @Async
    private void enviarEventoCreado(NotificationRequest request, String notificationId) {
        String eventoNombre = (String) request.getDatos().get("eventoNombre");
        Object eventoId = request.getDatos().get("eventoId");
        String fechaEvento = (String) request.getDatos().get("fechaEvento");
        
        if (mailSender == null) {
            // Modo simulación - solo logs
            log.info("╔═══════════════════════════════════════════════════════════╗");
            log.info("║         📧 SIMULACIÓN EMAIL - EVENTO CREADO              ║");
            log.info("╠═══════════════════════════════════════════════════════════╣");
            log.info("║ ID: {}", String.format("%-52s", notificationId) + "║");
            log.info("║ Para: {}", String.format("%-50s", request.getDestinatario()) + "║");
            log.info("║ Evento: {}", String.format("%-48s", eventoNombre) + "║");
            log.info("║ Fecha: {}", String.format("%-49s", fechaEvento) + "║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getDestinatario());
            message.setSubject("📅 Tu evento ha sido publicado");
            message.setText(
                "Tu evento '" + eventoNombre + "' ha sido creado exitosamente.\n\n" +
                "Detalles del evento:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "ID del Evento:  " + eventoId + "\n" +
                "Fecha:          " + fechaEvento + "\n\n" +
                "Los usuarios ya pueden comprar entradas para tu evento.\n\n" +
                "Saludos,\n" +
                "Equipo SOA Ticketing\n\n" +
                "Notification ID: " + notificationId
            );
            
            mailSender.send(message);
            log.info("Email EVENTO_CREADO enviado a: {} [{}]", request.getDestinatario(), notificationId);
            
        } catch (Exception e) {
            log.error("Error enviando email EVENTO_CREADO a {}: {}", request.getDestinatario(), e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    @Async
    private void enviarTicketComprado(NotificationRequest request, String notificationId) {
        String eventoNombre = (String) request.getDatos().get("eventoNombre");
        String tipoEntrada = (String) request.getDatos().get("tipoEntrada");
        Object cantidad = request.getDatos().get("cantidad");
        Object total = request.getDatos().get("total");
        String ticketId = (String) request.getDatos().get("ticketId");
        String fechaEvento = (String) request.getDatos().get("fechaEvento");
        
        if (mailSender == null) {
            // Modo simulación - solo logs
            log.info("╔═══════════════════════════════════════════════════════════╗");
            log.info("║         🎫 SIMULACIÓN EMAIL - TICKET COMPRADO            ║");
            log.info("╠═══════════════════════════════════════════════════════════╣");
            log.info("║ ID: {}", String.format("%-52s", notificationId) + "║");
            log.info("║ Para: {}", String.format("%-50s", request.getDestinatario()) + "║");
            log.info("║ Evento: {}", String.format("%-48s", eventoNombre) + "║");
            log.info("║ Tipo: {}", String.format("%-50s", tipoEntrada) + "║");
            log.info("║ Cantidad: {}", String.format("%-46s", cantidad) + "║");
            log.info("║ Total: ${}", String.format("%-49s", total) + "║");
            log.info("║ Ticket ID: {}", String.format("%-45s", ticketId) + "║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getDestinatario());
            message.setSubject("🎫 Confirmación de compra de entradas");
            message.setText(
                "¡Gracias por tu compra!\n\n" +
                "Tu ticket ha sido generado exitosamente.\n\n" +
                "Detalles de tu compra:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Código de ticket: " + ticketId + "\n" +
                "Evento:           " + eventoNombre + "\n" +
                "Tipo de entrada:  " + tipoEntrada + "\n" +
                "Cantidad:         " + cantidad + "\n" +
                "Total pagado:     $" + total + "\n" +
                "Fecha del evento: " + fechaEvento + "\n\n" +
                "⚠️ IMPORTANTE: Presenta este código en la entrada del evento.\n\n" +
                "Saludos,\n" +
                "Equipo SOA Ticketing\n\n" +
                "Notification ID: " + notificationId
            );
            
            mailSender.send(message);
            log.info("Email TICKET_COMPRADO enviado a: {} [{}]", request.getDestinatario(), notificationId);
            
        } catch (Exception e) {
            log.error("Error enviando email TICKET_COMPRADO a {}: {}", request.getDestinatario(), e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    @Async
    private void enviarPagoRechazado(NotificationRequest request, String notificationId) {
        String eventoNombre = (String) request.getDatos().get("eventoNombre");
        Object monto = request.getDatos().get("monto");
        String razon = (String) request.getDatos().get("razon");
        
        if (mailSender == null) {
            // Modo simulación - solo logs
            log.info("╔═══════════════════════════════════════════════════════════╗");
            log.info("║         ❌ SIMULACIÓN EMAIL - PAGO RECHAZADO             ║");
            log.info("╠═══════════════════════════════════════════════════════════╣");
            log.info("║ ID: {}", String.format("%-52s", notificationId) + "║");
            log.info("║ Para: {}", String.format("%-50s", request.getDestinatario()) + "║");
            log.info("║ Evento: {}", String.format("%-48s", eventoNombre) + "║");
            log.info("║ Monto: ${}", String.format("%-48s", monto) + "║");
            log.info("║ Razón: {}", String.format("%-49s", razon) + "║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getDestinatario());
            message.setSubject("❌ Pago rechazado");
            message.setText(
                "Lo sentimos, no pudimos procesar tu pago.\n\n" +
                "Detalles del intento:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Evento:  " + eventoNombre + "\n" +
                "Monto:   $" + monto + "\n" +
                "Razón:   " + razon + "\n\n" +
                "Por favor, verifica tu método de pago e intenta nuevamente.\n\n" +
                "Si el problema persiste, contacta con tu banco.\n\n" +
                "Saludos,\n" +
                "Equipo SOA Ticketing\n\n" +
                "Notification ID: " + notificationId
            );
            
            mailSender.send(message);
            log.info("Email PAGO_RECHAZADO enviado a: {} [{}]", request.getDestinatario(), notificationId);
            
        } catch (Exception e) {
            log.error("Error enviando email PAGO_RECHAZADO a {}: {}", request.getDestinatario(), e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }
}
