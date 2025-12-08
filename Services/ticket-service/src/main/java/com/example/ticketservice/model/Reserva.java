package com.example.ticketservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reservas")
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tipoEntradaId;
    
    @Column(nullable = false)
    private Long usuarioId;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(nullable = false)
    private Instant fechaCreacion;
    
    @Column(nullable = false)
    private Instant fechaExpiracion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;
    
    public enum EstadoReserva {
        ACTIVA,      // Reserva creada, stock decrementado, esperando pago
        CONFIRMADA,  // Pago exitoso, reserva confirmada
        LIBERADA,    // Pago fallido o tiempo expirado, stock restaurado
        CANCELADA    // Usuario canceló manualmente
    }
    
    // Constructors
    public Reserva() {
    }
    
    public Reserva(Long tipoEntradaId, Long usuarioId, Integer cantidad, Instant fechaExpiracion) {
        this.tipoEntradaId = tipoEntradaId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.fechaCreacion = Instant.now();
        this.fechaExpiracion = fechaExpiracion;
        this.estado = EstadoReserva.ACTIVA;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTipoEntradaId() {
        return tipoEntradaId;
    }
    
    public void setTipoEntradaId(Long tipoEntradaId) {
        this.tipoEntradaId = tipoEntradaId;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }
    
    public void setFechaExpiracion(Instant fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
    
    public EstadoReserva getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
}
