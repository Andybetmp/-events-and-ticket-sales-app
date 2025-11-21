package com.example.ticketservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true, nullable = false, length = 50)
    private String ticketId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo_entrada_id", nullable = false)
    private Long tipoEntradaId;

    @Column(name = "evento_nombre", nullable = false, length = 200)
    private String eventoNombre;

    @Column(name = "tipo_entrada_nombre", nullable = false, length = 100)
    private String tipoEntradaNombre;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "total_pagado", nullable = false)
    private Double totalPagado;

    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(nullable = false, length = 20)
    private String estado = "PAGADO"; // PAGADO, CANCELADO

    @Column(name = "fecha_compra", nullable = false, updatable = false)
    private Instant fechaCompra;

    @PrePersist
    protected void onCreate() {
        fechaCompra = Instant.now();
    }
}
