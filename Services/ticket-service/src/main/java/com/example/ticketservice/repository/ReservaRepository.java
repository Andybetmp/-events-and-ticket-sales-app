package com.example.ticketservice.repository;

import com.example.ticketservice.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    List<Reserva> findByUsuarioIdAndEstado(Long usuarioId, Reserva.EstadoReserva estado);
    
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'ACTIVA' AND r.fechaExpiracion < :now")
    List<Reserva> findReservasExpiradas(Instant now);
    
    List<Reserva> findByTipoEntradaIdAndEstado(Long tipoEntradaId, Reserva.EstadoReserva estado);
}
