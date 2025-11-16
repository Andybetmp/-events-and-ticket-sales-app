package com.example.eventservice.service;

import com.example.eventservice.dto.CreateTipoEntradaRequest;
import com.example.eventservice.dto.TipoEntradaDto;
import com.example.eventservice.dto.UpdateTipoEntradaRequest;
import com.example.eventservice.exception.BadRequestException;
import com.example.eventservice.exception.ResourceNotFoundException;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.TipoEntrada;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.TipoEntradaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEntradaService {

    private final TipoEntradaRepository tipoEntradaRepository;
    private final EventRepository eventRepository;

    @Transactional
    public TipoEntradaDto createTipoEntrada(Long eventoId, CreateTipoEntradaRequest request) {
        log.info("Creando tipo de entrada para evento {}: {}", eventoId, request.getNombre());

        // Verificar que el evento existe
        Event evento = eventRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventoId));

        // Verificar que no exista un tipo de entrada con el mismo nombre para este evento
        if (tipoEntradaRepository.existsByEventoIdAndNombre(eventoId, request.getNombre())) {
            throw new BadRequestException("Ya existe un tipo de entrada con el nombre '" + request.getNombre() + "' para este evento");
        }

        // Crear el tipo de entrada
        TipoEntrada tipoEntrada = TipoEntrada.builder()
                .evento(evento)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .cantidadTotal(request.getCantidad())
                .cantidadDisponible(request.getCantidad())
                .orden(request.getOrden() != null ? request.getOrden() : 0)
                .activo(true)
                .build();

        tipoEntrada = tipoEntradaRepository.save(tipoEntrada);
        log.info("Tipo de entrada creado exitosamente con id: {}", tipoEntrada.getId());

        return TipoEntradaDto.fromEntity(tipoEntrada);
    }

    @Transactional(readOnly = true)
    public List<TipoEntradaDto> getTiposEntradaByEvento(Long eventoId, boolean soloActivos) {
        log.info("Obteniendo tipos de entrada para evento {}, soloActivos: {}", eventoId, soloActivos);

        // Verificar que el evento existe
        if (!eventRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + eventoId);
        }

        List<TipoEntrada> tiposEntrada;
        if (soloActivos) {
            tiposEntrada = tipoEntradaRepository.findByEventoIdAndActivoTrueOrderByOrdenAsc(eventoId);
        } else {
            tiposEntrada = tipoEntradaRepository.findByEventoIdOrderByOrdenAsc(eventoId);
        }

        return tiposEntrada.stream()
                .map(TipoEntradaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoEntradaDto getTipoEntradaById(Long tipoEntradaId) {
        log.info("Obteniendo tipo de entrada con id: {}", tipoEntradaId);

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + tipoEntradaId));

        return TipoEntradaDto.fromEntity(tipoEntrada);
    }

    @Transactional
    public TipoEntradaDto updateTipoEntrada(Long tipoEntradaId, UpdateTipoEntradaRequest request) {
        log.info("Actualizando tipo de entrada con id: {}", tipoEntradaId);

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + tipoEntradaId));

        // Verificar nombre duplicado si se está cambiando el nombre
        if (request.getNombre() != null && !request.getNombre().equals(tipoEntrada.getNombre())) {
            if (tipoEntradaRepository.existsByEventoIdAndNombre(tipoEntrada.getEvento().getId(), request.getNombre())) {
                throw new BadRequestException("Ya existe un tipo de entrada con el nombre '" + request.getNombre() + "' para este evento");
            }
            tipoEntrada.setNombre(request.getNombre());
        }

        // Actualizar campos opcionales
        if (request.getDescripcion() != null) {
            tipoEntrada.setDescripcion(request.getDescripcion());
        }

        if (request.getPrecio() != null) {
            tipoEntrada.setPrecio(request.getPrecio());
        }

        if (request.getCantidadTotal() != null) {
            // Validar que la nueva cantidad total no sea menor a las entradas vendidas
            int entradasVendidas = tipoEntrada.getCantidadTotal() - tipoEntrada.getCantidadDisponible();
            if (request.getCantidadTotal() < entradasVendidas) {
                throw new BadRequestException("La cantidad total no puede ser menor a las entradas ya vendidas (" + entradasVendidas + ")");
            }

            // Ajustar la cantidad disponible proporcionalmente
            int diferencia = request.getCantidadTotal() - tipoEntrada.getCantidadTotal();
            tipoEntrada.setCantidadTotal(request.getCantidadTotal());
            tipoEntrada.setCantidadDisponible(tipoEntrada.getCantidadDisponible() + diferencia);
        }

        if (request.getCantidadDisponible() != null) {
            int entradasVendidas = tipoEntrada.getCantidadTotal() - tipoEntrada.getCantidadDisponible();
            if (request.getCantidadDisponible() + entradasVendidas > tipoEntrada.getCantidadTotal()) {
                throw new BadRequestException("La cantidad disponible excede la capacidad total");
            }
            tipoEntrada.setCantidadDisponible(request.getCantidadDisponible());
        }

        if (request.getOrden() != null) {
            tipoEntrada.setOrden(request.getOrden());
        }

        if (request.getActivo() != null) {
            tipoEntrada.setActivo(request.getActivo());
        }

        tipoEntrada = tipoEntradaRepository.save(tipoEntrada);
        log.info("Tipo de entrada actualizado exitosamente");

        return TipoEntradaDto.fromEntity(tipoEntrada);
    }

    @Transactional
    public void deleteTipoEntrada(Long tipoEntradaId) {
        log.info("Eliminando tipo de entrada con id: {}", tipoEntradaId);

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + tipoEntradaId));

        // Verificar que no haya entradas vendidas
        int entradasVendidas = tipoEntrada.getCantidadTotal() - tipoEntrada.getCantidadDisponible();
        if (entradasVendidas > 0) {
            throw new BadRequestException("No se puede eliminar un tipo de entrada que ya tiene ventas. Considere desactivarlo en su lugar.");
        }

        tipoEntradaRepository.delete(tipoEntrada);
        log.info("Tipo de entrada eliminado exitosamente");
    }

    @Transactional
    public void decreaseCantidad(Long tipoEntradaId, int cantidad) {
        log.info("Disminuyendo {} entradas del tipo de entrada con id: {}", cantidad, tipoEntradaId);

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + tipoEntradaId));

        if (!tipoEntrada.getActivo()) {
            throw new BadRequestException("El tipo de entrada no está activo");
        }

        if (tipoEntrada.getCantidadDisponible() < cantidad) {
            throw new BadRequestException("No hay suficientes entradas disponibles. Disponibles: " + tipoEntrada.getCantidadDisponible());
        }

        tipoEntrada.setCantidadDisponible(tipoEntrada.getCantidadDisponible() - cantidad);
        tipoEntradaRepository.save(tipoEntrada);

        log.info("Cantidad disminuida exitosamente. Disponibles ahora: {}", tipoEntrada.getCantidadDisponible());
    }

    @Transactional
    public void increaseCantidad(Long tipoEntradaId, int cantidad) {
        log.warn("⚠️ COMPENSACIÓN: Incrementando {} entradas al tipo de entrada con id: {} (ROLLBACK)", cantidad, tipoEntradaId);

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + tipoEntradaId));

        tipoEntrada.setCantidadDisponible(tipoEntrada.getCantidadDisponible() + cantidad);
        tipoEntradaRepository.save(tipoEntrada);

        log.warn("✓ Compensación completada. Disponibles restaurados a: {}", tipoEntrada.getCantidadDisponible());
    }
}
