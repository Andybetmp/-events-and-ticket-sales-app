package com.example.ticketservice.orchestrator;

import com.example.ticketservice.client.EventServiceClient;
import com.example.ticketservice.client.NotificationServiceClient;
import com.example.ticketservice.client.PaymentServiceClient;
import com.example.ticketservice.client.TicketServiceClient;
import com.example.ticketservice.dto.PurchaseTicketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Orquestador de Compra de Tickets - Implementa el Patrón SAGA con RESERVAS TEMPORALES.
 * 
 * PATRÓN SAGA CON RESERVAS:
 * En microservicios, NO podemos usar transacciones ACID tradicionales porque cada servicio
 * tiene su propia base de datos. El patrón Saga divide una transacción distribuida en
 * pasos secuenciales, y si algún paso falla, ejecuta "compensaciones" para deshacer 
 * los pasos previos exitosos.
 * 
 * FLUJO DE COMPRA CON RESERVAS (6 PASOS):
 * 1. Obtener información del tipo de entrada
 * 2. Obtener información del evento
 * 3. CREAR RESERVA temporal (decrementa stock por 10 minutos) ← PUNTO DE BLOQUEO
 * 4. Procesar pago ← PUNTO CRÍTICO (puede fallar)
 * 5. CONFIRMAR RESERVA y crear ticket (pago exitoso)
 * 6. Enviar notificación de confirmación
 * 
 * COMPENSACIÓN:
 * Si el pago falla después de crear la reserva:
 * - liberarReserva() para restaurar el stock
 * - Notificar al usuario del rechazo
 * 
 * Si el timer de 10 minutos expira, un @Scheduled job libera automáticamente la reserva.
 * 
 * IMPORTANTE:
 * Este servicio NO tiene lógica de negocio, solo COORDINA llamadas a otros servicios
 * via REST clients. Es un orquestador puro siguiendo principios de microservicios.
 */
@Service
public class TicketPurchaseOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TicketPurchaseOrchestrator.class);

    @Autowired
    private EventServiceClient eventClient;

    @Autowired
    private PaymentServiceClient paymentClient;

    @Autowired
    private NotificationServiceClient notificationClient;

    @Autowired
    private TicketServiceClient ticketClient;

    public Map<String, Object> orchestratePurchase(Long userId, String userEmail, PurchaseTicketRequest request) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("INICIANDO ORQUESTACIÓN DE COMPRA CON RESERVA TEMPORAL");
        log.info("Usuario ID: {}, Email: {}", userId, userEmail);
        log.info("═══════════════════════════════════════════════════════════");

        // Variables para compensación
        Long reservaId = null;
        Long tipoEntradaId = request.getTipoEntradaId();
        Integer cantidad = request.getCantidad();

        try {
            // PASO 1: Obtener información del tipo de entrada
            log.info("PASO 1: Obteniendo información del tipo de entrada ID={}", request.getTipoEntradaId());
            Map<String, Object> tipoEntrada = eventClient.getTipoEntrada(request.getTipoEntradaId());
            
            Integer cantidadDisponible = ((Number) tipoEntrada.get("cantidadDisponible")).intValue();
            Double precio = ((Number) tipoEntrada.get("precio")).doubleValue();
            String tipoNombre = (String) tipoEntrada.get("nombre");
            Long eventoId = ((Number) tipoEntrada.get("eventoId")).longValue();
            
            log.info("  ✓ Tipo: {}, Precio: ${}, Disponibles: {}", tipoNombre, precio, cantidadDisponible);

            // PASO 2: Obtener información del evento
            log.info("PASO 2: Obteniendo información del evento ID={}", eventoId);
            Map<String, Object> evento = eventClient.getEvento(eventoId);
            String eventoNombre = (String) evento.get("nombre");
            String fechaEvento = (String) evento.get("fechaEvento");
            log.info("  ✓ Evento: {}, Fecha: {}", eventoNombre, fechaEvento);

            // PASO 3: CREAR RESERVA TEMPORAL (decrementa stock por 10 minutos)
            log.info("PASO 3: CREANDO RESERVA TEMPORAL de {} entradas (expira en 10 min)", request.getCantidad());
            try {
                Map<String, Object> reserva = ticketClient.crearReserva(tipoEntradaId, userId, cantidad);
                reservaId = ((Number) reserva.get("id")).longValue();
                Long segundosRestantes = ((Number) reserva.get("segundosRestantes")).longValue();
                log.info("  ✓ Reserva ID={} creada exitosamente (expira en {} segundos)", reservaId, segundosRestantes);
                log.info("  ✓ Stock DECREMENTADO temporalmente - Usuario tiene tiempo limitado para pagar");
            } catch (Exception e) {
                log.error("  ✗ Error al crear reserva: {}", e.getMessage());
                throw new RuntimeException("No se pudo crear la reserva: " + e.getMessage());
            }

            // PASO 4: Procesar pago (operación crítica)
            Double montoTotal = precio * request.getCantidad();
            log.info("PASO 4: Procesando pago por ${} (CRÍTICO - puede fallar)", montoTotal);
            
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("idempotencyKey", request.getIdempotencyKey()); // NEW: For idempotency
            paymentRequest.put("monto", montoTotal);
            paymentRequest.put("cardNumber", request.getPaymentMethod().getCardNumber());
            paymentRequest.put("cvv", request.getPaymentMethod().getCvv());
            paymentRequest.put("expiryDate", request.getPaymentMethod().getExpiryDate());
            paymentRequest.put("cardHolder", request.getPaymentMethod().getCardHolder());
            
            Map<String, Object> paymentResponse;
            String paymentStatus;
            String paymentId;
            
            try {
                paymentResponse = paymentClient.authorize(paymentRequest);
                paymentStatus = (String) paymentResponse.get("status");
                paymentId = (String) paymentResponse.get("paymentId");
                
                if (!"APPROVED".equals(paymentStatus)) {
                    String mensaje = (String) paymentResponse.get("mensaje");
                    log.error("  ✗ Pago rechazado: {}", mensaje);
                    
                    // COMPENSACIÓN: Liberar reserva
                    log.warn("⚠️ Iniciando COMPENSACIÓN - Liberando reserva ID={}", reservaId);
                    ticketClient.liberarReserva(reservaId);
                    log.info("  ✓ Reserva liberada - Stock restaurado automáticamente");
                    
                    // Notificar pago rechazado
                    sendPaymentRejectedNotification(userEmail, eventoNombre, montoTotal, mensaje);
                    
                    throw new RuntimeException("Pago rechazado: " + mensaje);
                }
                log.info("  ✓ Pago aprobado. Payment ID: {}", paymentId);
                
            } catch (RuntimeException e) {
                // Si ya hicimos compensación dentro del bloque, re-lanzar
                throw e;
            } catch (Exception e) {
                log.error("  ✗ Error crítico procesando pago: {}", e.getMessage());
                
                // COMPENSACIÓN: Liberar reserva
                if (reservaId != null) {
                    log.warn("⚠️ Iniciando COMPENSACIÓN - Liberando reserva ID={}", reservaId);
                    try {
                        ticketClient.liberarReserva(reservaId);
                        log.info("  ✓ Reserva liberada - Stock restaurado");
                    } catch (Exception compensationError) {
                        log.error("  ✗✗ ERROR EN COMPENSACIÓN: {}", compensationError.getMessage());
                        // En producción: enviar alerta crítica, requiere intervención manual
                    }
                }
                throw new RuntimeException("Error procesando pago: " + e.getMessage());
            }

            // PASO 5: CONFIRMAR RESERVA y crear ticket
            log.info("PASO 5: CONFIRMANDO RESERVA ID={} y creando ticket", reservaId);
            Map<String, Object> ticket;
            try {
                // Confirmar reserva (cambia estado a CONFIRMADA)
                ticketClient.confirmarReserva(reservaId);
                log.info("  ✓ Reserva confirmada - Stock definitivamente vendido");
                
                // Crear ticket
                ticket = ticketClient.crearTicket(
                    userId,
                    request.getTipoEntradaId(),
                    eventoNombre,
                    tipoNombre,
                    request.getCantidad(),
                    precio,
                    paymentId
                );
                log.info("  ✓ Ticket creado: {}", ticket.get("ticketId"));
            } catch (Exception e) {
                log.error("  ✗ Error crítico confirmando reserva/creando ticket: {}", e.getMessage());
                
                // COMPENSACIÓN COMPLEJA: Reversar pago Y liberar reserva
                log.error("⚠️⚠️ COMPENSACIÓN CRÍTICA REQUERIDA");
                log.error("  1. Payment ID {} aprobado pero ticket no creado", paymentId);
                log.error("  2. Intentando liberar reserva ID={}", reservaId);
                
                try {
                    ticketClient.liberarReserva(reservaId);
                    log.info("  ✓ Reserva liberada - Stock restaurado");
                } catch (Exception compError) {
                    log.error("  ✗✗ ERROR liberando reserva: {}", compError.getMessage());
                }
                
                throw new RuntimeException("Error crítico: pago procesado pero ticket no creado. Payment ID: " + paymentId);
            }

            // PASO 6: Enviar notificación de confirmación (no crítico)
            log.info("PASO 6: Enviando notificación de confirmación");
            try {
                sendTicketPurchasedNotification(userEmail, ticket, eventoNombre, tipoNombre, fechaEvento);
                log.info("  ✓ Notificación enviada");
            } catch (Exception e) {
                log.warn("  ⚠ Notificación falló (no crítico): {}", e.getMessage());
                // No afecta la transacción principal
            }

            log.info("═══════════════════════════════════════════════════════════");
            log.info("✓ ORQUESTACIÓN COMPLETADA EXITOSAMENTE");
            log.info("  Reserva ID: {} (CONFIRMADA)", reservaId);
            log.info("  Ticket ID: {}", ticket.get("ticketId"));
            log.info("  Total pagado: ${}", ticket.get("total"));
            log.info("  Estado: VENTA CONFIRMADA");
            log.info("═══════════════════════════════════════════════════════════");

            return ticket;

        } catch (RuntimeException e) {
            // Error ya manejado con compensación
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ORQUESTACIÓN FALLIDA: {}", e.getMessage());
            if (reservaId != null) {
                log.error("  Reserva ID={} fue liberada (stock restaurado)", reservaId);
            }
            log.error("═══════════════════════════════════════════════════════════");
            throw e;
        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ERROR INESPERADO EN ORQUESTACIÓN: {}", e.getMessage());
            
            // Compensación de último recurso
            if (reservaId != null) {
                log.warn("⚠️ Compensación de emergencia - Liberando reserva ID={}", reservaId);
                try {
                    ticketClient.liberarReserva(reservaId);
                    log.info("  ✓ Reserva liberada - Stock restaurado");
                } catch (Exception compensationError) {
                    log.error("  ✗✗ ERROR EN COMPENSACIÓN DE EMERGENCIA: {}", compensationError.getMessage());
                }
            }
            log.error("═══════════════════════════════════════════════════════════");
            throw new RuntimeException("Error al procesar la compra: " + e.getMessage(), e);
        }
    }

    private void sendTicketPurchasedNotification(String email, Map<String, Object> ticket, String eventoNombre, 
                                                 String tipoNombre, String fechaEvento) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("tipo", "TICKET_COMPRADO");
        notification.put("destinatario", email);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("ticketId", ticket.get("ticketId"));
        datos.put("eventoNombre", eventoNombre);
        datos.put("tipoEntrada", tipoNombre);
        datos.put("cantidad", ticket.get("cantidad"));
        datos.put("total", ticket.get("total"));
        datos.put("fechaEvento", fechaEvento);
        
        notification.put("datos", datos);
        
        try {
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación: {}", e.getMessage());
        }
    }

    private void sendPaymentRejectedNotification(String email, String eventoNombre, Double monto, String razon) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("tipo", "PAGO_RECHAZADO");
        notification.put("destinatario", email);
        
        Map<String, Object> datos = new HashMap<>();
        datos.put("eventoNombre", eventoNombre);
        datos.put("monto", monto);
        datos.put("razon", razon);
        
        notification.put("datos", datos);
        
        try {
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de pago rechazado: {}", e.getMessage());
        }
    }
}
