package com.example.ticketservice.repository;

import com.example.ticketservice.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findByTicketId(String ticketId);
    
    List<Ticket> findByUsuarioId(Long usuarioId);
    
    List<Ticket> findByUsuarioIdAndEstado(Long usuarioId, String estado);
}
