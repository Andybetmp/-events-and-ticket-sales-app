package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> autorizarPago(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.procesarPago(request);
        
        if ("APPROVED".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(402).body(response); // Payment Required
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "service", "payment-service",
            "status", "UP",
            "description", "Servicio de pagos simulado",
            "reglas", Map.of(
                "rechazoPorMonto", "Montos > $1000 son rechazados",
                "rechazoPorTarjeta", "Tarjetas terminadas en 0000 son rechazadas (para testing de compensación)"
            )
        ));
    }
}
