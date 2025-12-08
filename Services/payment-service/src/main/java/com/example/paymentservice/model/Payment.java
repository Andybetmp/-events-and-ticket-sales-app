package com.example.paymentservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", unique = true, nullable = false, length = 50)
    private String paymentId;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey; // NEW: For idempotency checks

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false, length = 20)
    private String status; // APPROVED, REJECTED

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(length = 255)
    private String mensaje;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = Instant.now();
    }

    // Constructors
    public Payment() {
    }

    public Payment(String paymentId, String idempotencyKey, Double monto, String status, String cardLastFour, String mensaje) {
        this.paymentId = paymentId;
        this.idempotencyKey = idempotencyKey;
        this.monto = monto;
        this.status = status;
        this.cardLastFour = cardLastFour;
        this.mensaje = mensaje;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
