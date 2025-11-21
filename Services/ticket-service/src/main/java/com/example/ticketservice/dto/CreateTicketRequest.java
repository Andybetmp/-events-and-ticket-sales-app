package com.example.ticketservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    private Long usuarioId;
    private Long tipoEntradaId;
    private String eventoNombre;
    private String tipoEntradaNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private String paymentId;
}
