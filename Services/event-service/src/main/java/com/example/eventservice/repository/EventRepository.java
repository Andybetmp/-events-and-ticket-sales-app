package com.example.eventservice.repository;

import com.example.eventservice.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByActivoTrue();
    
    @Query("SELECT e FROM Event e WHERE e.fechaEvento >= :fechaDesde AND e.activo = true")
    List<Event> findEventosProximos(LocalDateTime fechaDesde);
    
    @Query("SELECT e FROM Event e WHERE e.entradasDisponibles > 0 AND e.activo = true")
    List<Event> findEventosConDisponibilidad();
}
