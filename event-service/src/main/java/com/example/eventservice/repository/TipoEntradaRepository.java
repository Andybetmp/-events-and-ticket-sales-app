package com.example.eventservice.repository;

import com.example.eventservice.model.TipoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoEntradaRepository extends JpaRepository<TipoEntrada, Long> {
    
    List<TipoEntrada> findByEventoIdAndActivoTrueOrderByOrdenAsc(Long eventoId);
    
    List<TipoEntrada> findByEventoIdOrderByOrdenAsc(Long eventoId);
    
    boolean existsByEventoIdAndNombre(Long eventoId, String nombre);
}
