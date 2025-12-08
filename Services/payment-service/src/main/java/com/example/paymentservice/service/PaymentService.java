package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de Procesamiento de Pagos (MOCK).
 * 
 * IMPORTANTE: Este es un MOCK para simular una pasarela de pago real.
 * 
 * REGLAS DE NEGOCIO SIMULADAS:
 * 1. Montos <= 0 → RECHAZADO
 * 2. Montos > $1000 → RECHAZADO (simula fondos insuficientes)
 * 3. Montos <= $1000 → APROBADO
 * 
 * RESPONSABILIDADES:
 * - Validar datos de la tarjeta
 * - Aplicar reglas de negocio de aprobación/rechazo
 * - Generar Payment ID único
 * - Registrar TODOS los intentos en BD (aprobados y rechazados)
 * - Retornar respuesta estructurada
 * 
 * EN PRODUCCIÓN REAL:
 * - Integrar con Stripe, PayPal, MercadoPago, etc.
 * - Encriptar datos sensibles de tarjeta
 * - Implementar 3D Secure
 * - Manejar webhooks de la pasarela
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final double LIMITE_MONTO = 1000.0; // Límite para simular fondos insuficientes

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse procesarPago(PaymentRequest request) {
        // IDEMPOTENCY CHECK: If idempotencyKey exists, return cached result
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isEmpty()) {
            var existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingPayment.isPresent()) {
                Payment cached = existingPayment.get();
                log.info("╔═══════════════════════════════════════════════════════════╗");
                log.info("║     ♻️ DUPLICATE PAYMENT DETECTED - RETURNING CACHED     ║");
                log.info("╠═══════════════════════════════════════════════════════════╣");
                log.info("║ Idempotency Key: {}", String.format("%-37s", request.getIdempotencyKey()) + "║");
                log.info("║ Cached Payment:  {}", String.format("%-37s", cached.getPaymentId()) + "║");
                log.info("║ Status:          {}", String.format("%-37s", cached.getStatus()) + "║");
                log.info("╚═══════════════════════════════════════════════════════════╝");
                return new PaymentResponse(
                    cached.getPaymentId(),
                    cached.getStatus(),
                    cached.getMonto(),
                    cached.getFechaCreacion(),
                    cached.getMensaje()
                );
            }
        }
        
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║           💳 PROCESANDO PAGO - PAYMENT SERVICE           ║");
        log.info("╠═══════════════════════════════════════════════════════════╣");
        log.info("║ Payment ID:       {}", String.format("%-39s", paymentId) + "║");
        log.info("║ Idempotency Key:  {}", String.format("%-39s", request.getIdempotencyKey() != null ? request.getIdempotencyKey() : "N/A") + "║");
        log.info("║ Monto:            ${}", String.format("%-38s", request.getMonto()) + "║");
        log.info("║ Tarjeta:          ****{}", String.format("%-35s", request.getCardNumber().substring(request.getCardNumber().length() - 4)) + "║");
        log.info("╠═══════════════════════════════════════════════════════════╣");
        
        // DEBUG: Mostrar número completo para verificar
        log.info("DEBUG: Número de tarjeta completo: {}", request.getCardNumber());
        log.info("DEBUG: ¿Termina en 0000? {}", request.getCardNumber().endsWith("0000"));
        
        // Validar monto (simulación de regla de negocio)
        if (request.getMonto() == null || request.getMonto() <= 0) {
            log.error("║ RESULTADO:        ❌ RECHAZADO - Monto inválido          ║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            
            // Guardar en BD
            Payment payment = new Payment(
                paymentId,
                request.getIdempotencyKey(),
                request.getMonto(),
                "REJECTED",
                request.getCardNumber() != null ? request.getCardNumber().substring(request.getCardNumber().length() - 4) : null,
                "Monto inválido"
            );
            paymentRepository.save(payment);
            
            return new PaymentResponse(
                paymentId,
                "REJECTED",
                request.getMonto(),
                Instant.now(),
                "Monto inválido"
            );
        }
        
        // Simular tarjeta inválida: rechazar tarjetas que terminan en 0000 (para testing de compensación)
        if (request.getCardNumber() != null && request.getCardNumber().endsWith("0000")) {
            log.warn("║ RESULTADO:        ❌ RECHAZADO - Tarjeta bloqueada       ║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            
            // Guardar en BD
            Payment payment = new Payment(
                paymentId,
                request.getIdempotencyKey(),
                request.getMonto(),
                "REJECTED",
                request.getCardNumber().substring(request.getCardNumber().length() - 4),
                "Tarjeta bloqueada por el banco"
            );
            paymentRepository.save(payment);
            
            return new PaymentResponse(
                paymentId,
                "REJECTED",
                request.getMonto(),
                Instant.now(),
                "Tarjeta bloqueada por el banco"
            );
        }
        
        // Simular validación de fondos: montos > 1000 son rechazados
        if (request.getMonto() > LIMITE_MONTO) {
            log.warn("║ RESULTADO:        ❌ RECHAZADO - Fondos insuficientes    ║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            
            // Guardar en BD
            Payment payment = new Payment(
                paymentId,
                request.getIdempotencyKey(),
                request.getMonto(),
                "REJECTED",
                request.getCardNumber().substring(request.getCardNumber().length() - 4),
                "Fondos insuficientes (monto supera límite de $" + LIMITE_MONTO + ")"
            );
            paymentRepository.save(payment);
            
            return new PaymentResponse(
                paymentId,
                "REJECTED",
                request.getMonto(),
                Instant.now(),
                "Fondos insuficientes (monto supera límite de $" + LIMITE_MONTO + ")"
            );
        }
        
        // Pago aprobado
        log.info("║ RESULTADO:        ✅ APROBADO                             ║");
        log.info("╚═══════════════════════════════════════════════════════════╝");
        
        // Guardar en BD
        Payment payment = new Payment(
            paymentId,
            request.getIdempotencyKey(),
            request.getMonto(),
            "APPROVED",
            request.getCardNumber().substring(request.getCardNumber().length() - 4),
            "Pago procesado exitosamente"
        );
        paymentRepository.save(payment);
        
        return new PaymentResponse(
            paymentId,
            "APPROVED",
            request.getMonto(),
            Instant.now(),
            "Pago procesado exitosamente"
        );
    }
    
    public Payment buscarPorPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId).orElse(null);
    }
}
