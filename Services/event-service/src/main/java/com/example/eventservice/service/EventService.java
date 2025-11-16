package com.example.eventservice.service;

import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.dto.EventDto;
import com.example.eventservice.dto.UpdateEventRequest;
import com.example.eventservice.exception.BadRequestException;
import com.example.eventservice.exception.ResourceNotFoundException;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.TipoEntrada;
import com.example.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventDto createEvent(CreateEventRequest request) {
        log.info("Creando evento: {}", request.getNombre());

        // Calcular capacidad total basada en los tipos de entrada
        int capacidadCalculada = request.getTiposEntrada().stream()
                .mapToInt(tipo -> tipo.getCantidad())
                .sum();

        // Crear el evento
        Event event = Event.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .ubicacion(request.getUbicacion())
                .fechaEvento(request.getFechaEvento())
                .capacidadTotal(capacidadCalculada)
                .categoria(request.getCategoria())
                .activo(true)
                .build();

        // Crear los tipos de entrada
        request.getTiposEntrada().forEach(tipoRequest -> {
            TipoEntrada tipoEntrada = TipoEntrada.builder()
                    .evento(event)
                    .nombre(tipoRequest.getNombre())
                    .descripcion(tipoRequest.getDescripcion())
                    .precio(tipoRequest.getPrecio())
                    .cantidadTotal(tipoRequest.getCantidad())
                    .cantidadDisponible(tipoRequest.getCantidad())
                    .orden(tipoRequest.getOrden() != null ? tipoRequest.getOrden() : 0)
                    .activo(true)
                    .build();
            event.addTipoEntrada(tipoEntrada);
        });

        Event savedEvent = eventRepository.save(event);
        log.info("Evento creado con ID: {} y {} tipos de entrada", savedEvent.getId(), savedEvent.getTiposEntrada().size());

        return EventDto.fromEntity(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventDto> getAllEvents() {
        log.info("Obteniendo todos los eventos");
        return eventRepository.findAll()
                .stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventDto> getActiveEvents() {
        log.info("Obteniendo eventos activos");
        return eventRepository.findByActivoTrue()
                .stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEvents() {
        log.info("Obteniendo eventos próximos");
        return eventRepository.findEventosProximos(LocalDateTime.now())
                .stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventDto> getEventsWithAvailability() {
        log.info("Obteniendo eventos con disponibilidad");
        return eventRepository.findEventosConDisponibilidad()
                .stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        log.info("Obteniendo evento con ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
        return EventDto.fromEntity(event);
    }

    @Transactional
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        log.info("Actualizando evento con ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));

        if (request.getNombre() != null) {
            event.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            event.setDescripcion(request.getDescripcion());
        }
        if (request.getUbicacion() != null) {
            event.setUbicacion(request.getUbicacion());
        }
        if (request.getFechaEvento() != null) {
            event.setFechaEvento(request.getFechaEvento());
        }
        if (request.getCategoria() != null) {
            event.setCategoria(request.getCategoria());
        }
        if (request.getActivo() != null) {
            event.setActivo(request.getActivo());
        }

        event.setFechaActualizacion(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        log.info("Evento actualizado: {}", updatedEvent.getId());

        return EventDto.fromEntity(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        log.info("Eliminando evento con ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));

        // Soft delete
        event.setActivo(false);
        event.setFechaActualizacion(LocalDateTime.now());
        eventRepository.save(event);

        log.info("Evento eliminado (soft delete): {}", id);
    }
}
