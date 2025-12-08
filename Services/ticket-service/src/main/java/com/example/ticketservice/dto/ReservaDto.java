package com.example.ticketservice.dto;

import com.example.ticketservice.model.Reserva;
import java.time.Instant;

public class ReservaDto {
    private Long id;
    private Long tipoEntradaId;
    private Long usuarioId;
    private Integer cantidad;
    private Instant fechaCreacion;
    private Instant fechaExpiracion;
    private String estado;
    private Long segundosRestantes;
    
    // Constructor desde entidad
    public static ReservaDto fromEntity(Reserva reserva) {
        ReservaDto dto = new ReservaDto();
        dto.setId(reserva.getId());
        dto.setTipoEntradaId(reserva.getTipoEntradaId());
        dto.setUsuarioId(reserva.getUsuarioId());
        dto.setCantidad(reserva.getCantidad());
        dto.setFechaCreacion(reserva.getFechaCreacion());
        dto.setFechaExpiracion(reserva.getFechaExpiracion());
        dto.setEstado(reserva.getEstado().name());
        
        // Calcular segundos restantes
        if (reserva.getEstado() == Reserva.EstadoReserva.ACTIVA) {
            long segundos = reserva.getFechaExpiracion().getEpochSecond() - Instant.now().getEpochSecond();
            dto.setSegundosRestantes(Math.max(0, segundos));
        } else {
            dto.setSegundosRestantes(0L);
        }
        
        return dto;
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Long getSegundosRestantes() {
        return segundosRestantes;
    }
    
    public void setSegundosRestantes(Long segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }
}
