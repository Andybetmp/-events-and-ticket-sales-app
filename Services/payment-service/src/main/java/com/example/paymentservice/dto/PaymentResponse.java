package com.example.paymentservice.dto;

import java.time.Instant;

public class PaymentResponse {
    private String paymentId;
    private String status; // APPROVED, REJECTED
    private Double monto;
    private Instant timestamp;
    private String mensaje;

    public PaymentResponse() {
    }

    public PaymentResponse(String paymentId, String status, Double monto, Instant timestamp, String mensaje) {
        this.paymentId = paymentId;
        this.status = status;
        this.monto = monto;
        this.timestamp = timestamp;
        this.mensaje = mensaje;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.parse(timestamp);
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
