package com.example.ticketservice.dto;

import jakarta.validation.constraints.*;

public class CreateReservaRequest {
    @NotNull(message = "El tipo de entrada es requerido")
    private Long tipoEntradaId;
    
    @NotNull(message = "El usuario es requerido")
    private Long usuarioId;
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 10, message = "No se pueden reservar más de 10 tickets")
    private Integer cantidad;
    
    // Constructors
    public CreateReservaRequest() {
    }
    
    public CreateReservaRequest(Long tipoEntradaId, Long usuarioId, Integer cantidad) {
        this.tipoEntradaId = tipoEntradaId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
    }
    
    // Getters and Setters
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
}
