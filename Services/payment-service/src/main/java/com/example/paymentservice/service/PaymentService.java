package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final double LIMITE_MONTO = 1000.0;

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentResponse procesarPago(PaymentRequest request) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║           💳 PROCESANDO PAGO - PAYMENT SERVICE           ║");
        log.info("╠═══════════════════════════════════════════════════════════╣");
        log.info("║ Payment ID:       {}", String.format("%-39s", paymentId) + "║");
        log.info("║ Monto:            ${}", String.format("%-38s", request.getMonto()) + "║");
        log.info("║ Tarjeta:          ****{}", String.format("%-35s", request.getCardNumber().substring(request.getCardNumber().length() - 4)) + "║");
        log.info("╠═══════════════════════════════════════════════════════════╣");
        
        // Validar monto (simulación de regla de negocio)
        if (request.getMonto() == null || request.getMonto() <= 0) {
            log.error("║ RESULTADO:        ❌ RECHAZADO - Monto inválido          ║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            
            // Guardar en BD
            Payment payment = new Payment(
                paymentId,
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
        
        // Simular validación de fondos: montos > 1000 son rechazados
        if (request.getMonto() > LIMITE_MONTO) {
            log.warn("║ RESULTADO:        ❌ RECHAZADO - Fondos insuficientes    ║");
            log.info("╚═══════════════════════════════════════════════════════════╝");
            
            // Guardar en BD
            Payment payment = new Payment(
                paymentId,
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
