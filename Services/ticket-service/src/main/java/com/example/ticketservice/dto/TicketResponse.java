package com.example.ticketservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private String ticketId;
    private String eventoNombre;
    private String tipoEntrada;
    private Integer cantidad;
    private Double precioUnitario;
    private Double total;
    private String paymentId;
    private String estado;
    private Instant fechaCompra;
}
