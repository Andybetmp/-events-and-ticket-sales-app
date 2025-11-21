package com.example.eventservice.dto;

import com.example.eventservice.model.TipoEntrada;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información del tipo de entrada")
public class TipoEntradaDto {

    @Schema(description = "ID del tipo de entrada", example = "1")
    private Long id;

    @Schema(description = "ID del evento", example = "1")
    private Long eventoId;

    @Schema(description = "Nombre del tipo de entrada", example = "VIP Gold")
    private String nombre;

    @Schema(description = "Descripción", example = "Acceso VIP con bebidas incluidas")
    private String descripcion;

    @Schema(description = "Precio", example = "150.00")
    private BigDecimal precio;

    @Schema(description = "Cantidad total", example = "500")
    private Integer cantidadTotal;

    @Schema(description = "Cantidad disponible", example = "450")
    private Integer cantidadDisponible;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer orden;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de actualización")
    private LocalDateTime fechaActualizacion;

    public static TipoEntradaDto fromEntity(TipoEntrada tipoEntrada) {
        return TipoEntradaDto.builder()
                .id(tipoEntrada.getId())
                .eventoId(tipoEntrada.getEvento().getId())
                .nombre(tipoEntrada.getNombre())
                .descripcion(tipoEntrada.getDescripcion())
                .precio(tipoEntrada.getPrecio())
                .cantidadTotal(tipoEntrada.getCantidadTotal())
                .cantidadDisponible(tipoEntrada.getCantidadDisponible())
                .orden(tipoEntrada.getOrden())
                .activo(tipoEntrada.getActivo())
                .fechaCreacion(tipoEntrada.getFechaCreacion())
                .fechaActualizacion(tipoEntrada.getFechaActualizacion())
                .build();
    }
}
