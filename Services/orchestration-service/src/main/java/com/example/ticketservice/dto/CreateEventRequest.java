package com.example.ticketservice.dto;

import lombok.Data;

@Data
public class CreateEventRequest {
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String fechaEvento;
    private String horaEvento;
    private Boolean activo;
}
