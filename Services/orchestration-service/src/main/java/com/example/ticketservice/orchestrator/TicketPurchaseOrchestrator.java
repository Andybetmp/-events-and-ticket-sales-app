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
 * Orquestador de Compra de Tickets - Implementa el Patrón SAGA con compensación.
 * 
 * PATRÓN SAGA:
 * En microservicios, NO podemos usar transacciones ACID tradicionales porque cada servicio
 * tiene su propia base de datos. El patrón Saga divide una transacción distribuida en
 * pasos secuenciales, y si algún paso falla, ejecuta "compensaciones" para deshacer 
 * los pasos previos exitosos.
 * 
 * FLUJO DE COMPRA (7 PASOS):
 * 1. Obtener información del tipo de entrada
 * 2. Validar stock disponible
 * 3. Obtener información del evento
 * 4. RESERVAR entradas (decrementar stock) ← COMPENSABLE
 * 5. Procesar pago ← PUNTO CRÍTICO (puede fallar)
 * 6. Crear ticket (confirmar reserva)
 * 7. Enviar notificación de confirmación
 * 
 * COMPENSACIÓN:
 * Si el pago falla después de decrementar stock, ejecutamos compensación:
 * - incrementarCantidad() para restaurar el stock
 * - Notificar al usuario del rechazo
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
        log.info("INICIANDO ORQUESTACIÓN DE COMPRA DE TICKET (CON SAGA)");
        log.info("Usuario ID: {}, Email: {}", userId, userEmail);
        log.info("═══════════════════════════════════════════════════════════");

        // Variables para compensación
        boolean stockReservado = false;
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

            // PASO 2: Validar stock disponible
            log.info("PASO 2: Validando stock (solicitado: {}, disponible: {})", request.getCantidad(), cantidadDisponible);
            if (cantidadDisponible < request.getCantidad()) {
                throw new RuntimeException("Stock insuficiente. Disponibles: " + cantidadDisponible);
            }
            log.info("  ✓ Stock suficiente");

            // PASO 3: Obtener información del evento
            log.info("PASO 3: Obteniendo información del evento ID={}", eventoId);
            Map<String, Object> evento = eventClient.getEvento(eventoId);
            String eventoNombre = (String) evento.get("nombre");
            String fechaEvento = (String) evento.get("fechaEvento");
            log.info("  ✓ Evento: {}, Fecha: {}", eventoNombre, fechaEvento);

            // PASO 4: RESERVAR entradas (decrementar stock temporalmente)
            log.info("PASO 4: RESERVANDO {} entradas (decrementando stock temporalmente)", request.getCantidad());
            try {
                eventClient.decreaseCantidad(request.getTipoEntradaId(), request.getCantidad());
                stockReservado = true;
                log.info("  ✓ Entradas RESERVADAS - Stock decrementado temporalmente");
            } catch (Exception e) {
                log.error("  ✗ Error al reservar entradas: {}", e.getMessage());
                throw new RuntimeException("No se pudo reservar las entradas: " + e.getMessage());
            }

            // PASO 5: Procesar pago (operación crítica)
            Double montoTotal = precio * request.getCantidad();
            log.info("PASO 5: Procesando pago por ${} (CRÍTICO - puede fallar)", montoTotal);
            
            Map<String, Object> paymentRequest = new HashMap<>();
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
                    log.warn("⚠️ Iniciando COMPENSACIÓN - Liberando reserva de {} entradas", cantidad);
                    eventClient.increaseCantidad(tipoEntradaId, cantidad);
                    stockReservado = false;
                    log.info("  ✓ Reserva liberada - Stock restaurado");
                    
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
                if (stockReservado) {
                    log.warn("⚠️ Iniciando COMPENSACIÓN - Liberando reserva de {} entradas", cantidad);
                    try {
                        eventClient.increaseCantidad(tipoEntradaId, cantidad);
                        log.info("  ✓ Reserva liberada - Stock restaurado");
                    } catch (Exception compensationError) {
                        log.error("  ✗✗ ERROR EN COMPENSACIÓN: {}", compensationError.getMessage());
                        // En producción: enviar alerta crítica, requiere intervención manual
                    }
                }
                throw new RuntimeException("Error procesando pago: " + e.getMessage());
            }

            // PASO 6: Crear registro de ticket (confirmar reserva)
            log.info("PASO 6: Creando registro de ticket - CONFIRMANDO RESERVA");
            Map<String, Object> ticket;
            try {
                ticket = ticketClient.crearTicket(
                    userId,
                    request.getTipoEntradaId(),
                    eventoNombre,
                    tipoNombre,
                    request.getCantidad(),
                    precio,
                    paymentId
                );
                log.info("  ✓ Ticket creado: {} - Reserva CONFIRMADA", ticket.get("ticketId"));
            } catch (Exception e) {
                log.error("  ✗ Error crítico creando ticket: {}", e.getMessage());
                
                // COMPENSACIÓN COMPLEJA: Reversar pago Y liberar stock
                log.error("⚠️⚠️ COMPENSACIÓN CRÍTICA REQUERIDA");
                log.error("  1. Payment ID {} aprobado pero ticket no creado", paymentId);
                log.error("  2. Se requiere reversar pago O mantener registro para auditoría");
                
                // En un sistema real: intentar reversar el pago o registrar en tabla de compensación
                // Por ahora: NO liberamos stock porque el pago fue exitoso
                // Esto requiere intervención manual o proceso batch de reconciliación
                
                throw new RuntimeException("Error crítico: pago procesado pero ticket no creado. Payment ID: " + paymentId);
            }

            // PASO 7: Enviar notificación de confirmación (no crítico)
            log.info("PASO 7: Enviando notificación de confirmación");
            try {
                sendTicketPurchasedNotification(userEmail, ticket, eventoNombre, tipoNombre, fechaEvento);
                log.info("  ✓ Notificación enviada");
            } catch (Exception e) {
                log.warn("  ⚠ Notificación falló (no crítico): {}", e.getMessage());
                // No afecta la transacción principal
            }

            log.info("═══════════════════════════════════════════════════════════");
            log.info("✓ ORQUESTACIÓN COMPLETADA EXITOSAMENTE");
            log.info("  Ticket ID: {}", ticket.get("ticketId"));
            log.info("  Total pagado: ${}", ticket.get("total"));
            log.info("  Estado: CONFIRMADO (reserva convertida en venta)");
            log.info("═══════════════════════════════════════════════════════════");

            return ticket;

        } catch (RuntimeException e) {
            // Error ya manejado con compensación
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ORQUESTACIÓN FALLIDA: {}", e.getMessage());
            log.error("═══════════════════════════════════════════════════════════");
            throw e;
        } catch (Exception e) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("✗ ERROR INESPERADO EN ORQUESTACIÓN: {}", e.getMessage());
            
            // Compensación de último recurso
            if (stockReservado) {
                log.warn("⚠️ Compensación de emergencia - Liberando {} entradas", cantidad);
                try {
                    eventClient.increaseCantidad(tipoEntradaId, cantidad);
                    log.info("  ✓ Stock restaurado");
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
