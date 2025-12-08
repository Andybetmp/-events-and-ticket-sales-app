package com.example.eventservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para crear un evento")
public class CreateEventRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre del evento", example = "Concierto de Rock", required = true)
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Schema(description = "Descripción del evento", example = "Gran concierto con las mejores bandas", required = true)
    private String descripcion;

    @NotBlank(message = "La ubicación es requerida")
    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    @Schema(description = "Ubicación del evento", example = "Estadio Nacional", required = true)
    private String ubicacion;

    @NotNull(message = "La fecha del evento es requerida")
    @Future(message = "La fecha del evento debe ser futura")
    @Schema(description = "Fecha y hora del evento", example = "2025-12-31T20:00:00", required = true)
    private LocalDateTime fechaEvento;

    @NotBlank(message = "La categoría es requerida")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Schema(description = "Categoría del evento", example = "Música", required = true)
    private String categoria;

    @Schema(description = "ID del usuario organizador", example = "1")
    private Long organizadorId;

    @Schema(description = "Nombre del organizador", example = "Juan Pérez")
    private String organizador;

    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    @Schema(description = "URL de la imagen del evento", example = "http://localhost:8087/uploads/imagen.jpg")
    private String imagenUrl;

    @NotNull(message = "Los tipos de entrada son requeridos")
    @NotEmpty(message = "Debe incluir al menos un tipo de entrada")
    @Valid
    @Schema(description = "Lista de tipos de entrada para el evento", required = true)
    private List<CreateTipoEntradaRequest> tiposEntrada;
}
