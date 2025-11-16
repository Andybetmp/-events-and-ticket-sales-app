package com.example.ticketservice.dto;

import java.time.Instant;

public class TicketResponse {
    private String ticketId;
    private String eventoNombre;
    private String tipoEntrada;
    private Integer cantidad;
    private Double precioUnitario;
    private Double total;
    private String paymentId;
    private String estado;
    private Instant fechaCompra;

    public TicketResponse() {
    }

    public TicketResponse(String ticketId, String eventoNombre, String tipoEntrada, 
                          Integer cantidad, Double precioUnitario, Double total, 
                          String paymentId, String estado, Instant fechaCompra) {
        this.ticketId = ticketId;
        this.eventoNombre = eventoNombre;
        this.tipoEntrada = tipoEntrada;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.paymentId = paymentId;
        this.estado = estado;
        this.fechaCompra = fechaCompra;
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventoNombre() {
        return eventoNombre;
    }

    public void setEventoNombre(String eventoNombre) {
        this.eventoNombre = eventoNombre;
    }

    public String getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(String tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
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

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
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
