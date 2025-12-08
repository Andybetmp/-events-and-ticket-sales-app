package com.example.eventservice.dto;

import com.example.eventservice.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información del evento")
public class EventDto {

    @Schema(description = "ID del evento", example = "1")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Concierto de Rock")
    private String nombre;

    @Schema(description = "Descripción del evento", example = "Gran concierto con las mejores bandas")
    private String descripcion;

    @Schema(description = "Ubicación del evento", example = "Estadio Nacional")
    private String ubicacion;

    @Schema(description = "Fecha y hora del evento", example = "2025-12-31T20:00:00")
    private LocalDateTime fechaEvento;

    @Schema(description = "Capacidad total", example = "5000")
    private Integer capacidadTotal;

    @Schema(description = "Entradas disponibles", example = "4500")
    private Integer entradasDisponibles;

    @Schema(description = "Tipos de entrada del evento")
    private List<TipoEntradaDto> tiposEntrada;

    @Schema(description = "Categoría del evento", example = "Música")
    private String categoria;

    @Schema(description = "ID del organizador", example = "1")
    private Long organizadorId;

    @Schema(description = "Nombre del organizador", example = "Juan Pérez")
    private String organizador;

    @Schema(description = "URL de la imagen del evento")
    private String imagenUrl;

    @Schema(description = "Estado del evento", example = "true")
    private Boolean activo;

    @Schema(description = "Estado del evento", example = "ACTIVO", allowableValues = {"ACTIVO", "CANCELADO", "FINALIZADO"})
    private String estado;

    @Schema(description = "Fecha de creación")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de actualización")
    private LocalDateTime fechaActualizacion;

    public static EventDto fromEntity(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .nombre(event.getNombre())
                .descripcion(event.getDescripcion())
                .ubicacion(event.getUbicacion())
                .fechaEvento(event.getFechaEvento())
                .capacidadTotal(event.getCapacidadTotal())
                .entradasDisponibles(event.getEntradasDisponibles())
                .tiposEntrada(event.getTiposEntrada() != null ?
                        event.getTiposEntrada().stream()
                                .map(TipoEntradaDto::fromEntity)
                                .collect(Collectors.toList()) :
                        List.of())
                .categoria(event.getCategoria())
                .organizadorId(event.getOrganizadorId())
                .organizador(event.getOrganizador())
                .imagenUrl(event.getImagenUrl())
                .activo(event.getActivo())
                .estado(event.getEstado() != null ? event.getEstado().name() : null)
                .fechaCreacion(event.getFechaCreacion())
                .fechaActualizacion(event.getFechaActualizacion())
                .build();
    }
}
