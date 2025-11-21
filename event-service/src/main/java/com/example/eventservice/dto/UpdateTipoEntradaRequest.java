package com.example.eventservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para actualizar un tipo de entrada")
public class UpdateTipoEntradaRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del tipo de entrada", example = "VIP Gold")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción del tipo de entrada", example = "Acceso VIP con bebidas incluidas")
    private String descripcion;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del tipo de entrada", example = "150.00")
    private BigDecimal precio;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Schema(description = "Cantidad total de entradas de este tipo", example = "500")
    private Integer cantidadTotal;

    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    @Schema(description = "Cantidad disponible", example = "450")
    private Integer cantidadDisponible;

    @Min(value = 1, message = "El orden debe ser al menos 1")
    @Schema(description = "Orden de visualización", example = "1")
    private Integer orden;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;
}
