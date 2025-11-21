package com.example.ticketservice.service;

import com.example.ticketservice.dto.CreateTicketRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.model.Ticket;
import com.example.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Gestión de Tickets - Dueño del Dominio "Ticket".
 * 
 * RESPONSABILIDADES (Domain-Driven Design):
 * - Crear tickets cuando una compra es confirmada
 * - Generar Ticket ID único (formato: TKT-XXXXXXXX)
 * - Persistir en base de datos exclusiva
 * - Consultar tickets por usuario, ID o listar todos
 * 
 * IMPORTANTE - SEPARACIÓN DE SERVICIOS:
 * Este servicio es el ÚNICO que:
 * - Conoce el modelo Ticket
 * - Accede a la tabla tickets
 * - Maneja la lógica de negocio de tickets
 * 
 * Orchestration-service NO tiene acceso directo a tickets,
 * debe llamar a este servicio vía REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse crearTicket(CreateTicketRequest request) {
        log.info("Creando ticket para usuario ID: {}", request.getUsuarioId());
        
        Ticket ticket = Ticket.builder()
                .ticketId("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .usuarioId(request.getUsuarioId())
                .tipoEntradaId(request.getTipoEntradaId())
                .eventoNombre(request.getEventoNombre())
                .tipoEntradaNombre(request.getTipoEntradaNombre())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .totalPagado(request.getPrecioUnitario() * request.getCantidad())
                .paymentId(request.getPaymentId())
                .estado("PAGADO")
                .build();
        
        ticket = ticketRepository.save(ticket);
        log.info("Ticket creado: {}", ticket.getTicketId());
        
        return toResponse(ticket);
    }

    public TicketResponse obtenerTicketPorId(String ticketId) {
        log.info("Buscando ticket: {}", ticketId);
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
        return toResponse(ticket);
    }

    public List<TicketResponse> obtenerTicketsPorUsuario(Long usuarioId) {
        log.info("Obteniendo tickets del usuario ID: {}", usuarioId);
        List<Ticket> tickets = ticketRepository.findByUsuarioId(usuarioId);
        return tickets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> obtenerTodosLosTickets() {
        log.info("Obteniendo todos los tickets");
        return ticketRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .eventoNombre(ticket.getEventoNombre())
                .tipoEntrada(ticket.getTipoEntradaNombre())
                .cantidad(ticket.getCantidad())
                .precioUnitario(ticket.getPrecioUnitario())
                .total(ticket.getTotalPagado())
                .paymentId(ticket.getPaymentId())
                .estado(ticket.getEstado())
                .fechaCompra(ticket.getFechaCompra())
                .build();
    }
}
