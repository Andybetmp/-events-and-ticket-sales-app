package com.example.ticketservice.service;

import com.example.ticketservice.dto.CreateTicketRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.exception.TicketNotFoundException;
import com.example.ticketservice.exception.UnauthorizedAccessException;
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
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado: " + ticketId));
        return toResponse(ticket);
    }

    /**
     * Obtiene un ticket validando que el usuario tenga permiso para verlo.
     * Solo el propietario o un ADMIN puede ver un ticket.
     */
    public TicketResponse obtenerTicketPorIdConValidacion(String ticketId, Long userId, String userRole) {
        log.info("Buscando ticket: {} | Usuario: {} | Rol: {}", ticketId, userId, userRole);
        
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado: " + ticketId));
        
        // Validar ownership: solo el dueño o ADMIN puede ver el ticket
        if (!esAdmin(userRole) && !ticket.getUsuarioId().equals(userId)) {
            log.warn("Acceso denegado: Usuario {} intentó acceder al ticket {} del usuario {}", 
                userId, ticketId, ticket.getUsuarioId());
            throw new UnauthorizedAccessException(
                "No tienes permiso para ver este ticket. Solo puedes ver tus propios tickets.");
        }
        
        return toResponse(ticket);
    }

    public List<TicketResponse> obtenerTicketsPorUsuario(Long usuarioId) {
        log.info("Obteniendo tickets del usuario ID: {}", usuarioId);
        List<Ticket> tickets = ticketRepository.findByUsuarioId(usuarioId);
        return tickets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los tickets de un usuario validando permisos.
     * Solo el mismo usuario o un ADMIN puede ver los tickets.
     */
    public List<TicketResponse> obtenerTicketsPorUsuarioConValidacion(Long usuarioId, Long requestUserId, String userRole) {
        log.info("Obteniendo tickets del usuario {} | Solicitante: {} | Rol: {}", usuarioId, requestUserId, userRole);
        
        // Validar que sea el mismo usuario o ADMIN
        if (!esAdmin(userRole) && !usuarioId.equals(requestUserId)) {
            log.warn("Acceso denegado: Usuario {} intentó acceder a los tickets del usuario {}", 
                requestUserId, usuarioId);
            throw new UnauthorizedAccessException(
                "No tienes permiso para ver los tickets de otro usuario.");
        }
        
        return obtenerTicketsPorUsuario(usuarioId);
    }

    public List<TicketResponse> obtenerTodosLosTickets() {
        log.info("Obteniendo todos los tickets");
        return ticketRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los tickets del sistema.
     * Solo ADMIN puede ejecutar esta operación.
     */
    public List<TicketResponse> obtenerTodosLosTicketsConValidacion(String userRole) {
        log.info("Obteniendo todos los tickets | Rol: {}", userRole);
        
        if (!esAdmin(userRole)) {
            log.warn("Acceso denegado: Usuario sin rol ADMIN intentó listar todos los tickets");
            throw new UnauthorizedAccessException(
                "Solo los administradores pueden ver todos los tickets del sistema.");
        }
        
        return obtenerTodosLosTickets();
    }

    /**
     * Verifica si el rol del usuario es ADMIN.
     */
    private boolean esAdmin(String userRole) {
        return "ADMIN".equalsIgnoreCase(userRole);
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
