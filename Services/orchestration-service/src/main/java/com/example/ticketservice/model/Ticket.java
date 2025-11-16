package com.example.ticketservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tickets")
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

    // Constructors
    public Ticket() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getTipoEntradaId() {
        return tipoEntradaId;
    }

    public void setTipoEntradaId(Long tipoEntradaId) {
        this.tipoEntradaId = tipoEntradaId;
    }

    public String getEventoNombre() {
        return eventoNombre;
    }

    public void setEventoNombre(String eventoNombre) {
        this.eventoNombre = eventoNombre;
    }

    public String getTipoEntradaNombre() {
        return tipoEntradaNombre;
    }

    public void setTipoEntradaNombre(String tipoEntradaNombre) {
        this.tipoEntradaNombre = tipoEntradaNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(Double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Instant fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
}
