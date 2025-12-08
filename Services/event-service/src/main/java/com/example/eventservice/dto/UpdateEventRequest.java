package com.example.eventservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para actualizar un evento")
public class UpdateEventRequest {

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre del evento", example = "Concierto de Rock")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Schema(description = "Descripción del evento", example = "Gran concierto con las mejores bandas")
    private String descripcion;

    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    @Schema(description = "Ubicación del evento", example = "Estadio Nacional")
    private String ubicacion;

    @Future(message = "La fecha del evento debe ser futura")
    @Schema(description = "Fecha y hora del evento", example = "2025-12-31T20:00:00")
    private LocalDateTime fechaEvento;

    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Schema(description = "Categoría del evento", example = "Música")
    private String categoria;

    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    @Schema(description = "URL de la imagen del evento")
    private String imagenUrl;

    @Schema(description = "Estado del evento", example = "true")
    private Boolean activo;

    @Schema(description = "Estado del evento", example = "ACTIVO", allowableValues = {"ACTIVO", "CANCELADO", "FINALIZADO"})
    private String estado;
}
