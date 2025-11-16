package com.example.ticketservice.service;

import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.model.Ticket;
import com.example.ticketservice.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public Ticket crearTicket(Long usuarioId, Long tipoEntradaId, String eventoNombre, 
                             String tipoEntradaNombre, Integer cantidad, 
                             Double precioUnitario, String paymentId) {
        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setUsuarioId(usuarioId);
        ticket.setTipoEntradaId(tipoEntradaId);
        ticket.setEventoNombre(eventoNombre);
        ticket.setTipoEntradaNombre(tipoEntradaNombre);
        ticket.setCantidad(cantidad);
        ticket.setPrecioUnitario(precioUnitario);
        ticket.setTotalPagado(precioUnitario * cantidad);
        ticket.setPaymentId(paymentId);
        ticket.setEstado("PAGADO");
        
        return ticketRepository.save(ticket);
    }

    public List<TicketResponse> obtenerTicketsPorUsuario(Long usuarioId) {
        List<Ticket> tickets = ticketRepository.findByUsuarioId(usuarioId);
        return tickets.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getTicketId(),
            ticket.getEventoNombre(),
            ticket.getTipoEntradaNombre(),
            ticket.getCantidad(),
            ticket.getPrecioUnitario(),
            ticket.getTotalPagado(),
            ticket.getPaymentId(),
            ticket.getEstado(),
            ticket.getFechaCompra()
        );
    }
}
