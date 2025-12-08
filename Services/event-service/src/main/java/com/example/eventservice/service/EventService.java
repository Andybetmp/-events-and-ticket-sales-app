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

        // Validar que la fecha del evento sea futura
        if (request.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha del evento debe ser futura");
        }

        // Validar que haya al menos un tipo de entrada
        if (request.getTiposEntrada() == null || request.getTiposEntrada().isEmpty()) {
            throw new BadRequestException("El evento debe tener al menos un tipo de entrada");
        }

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
                .organizadorId(request.getOrganizadorId())
                .organizador(request.getOrganizador())
                .imagenUrl(request.getImagenUrl())
                .estado(Event.EstadoEvento.ACTIVO)
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
        return eventRepository.findAll()
                .stream()
                .filter(e -> e.getEstado() == Event.EstadoEvento.ACTIVO)
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
    public EventDto updateEvent(Long id, UpdateEventRequest request, Long userId, String userRole) {
        log.info("Actualizando evento con ID: {} por usuario: {}", id, userId);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));

        // Validar que el usuario sea el organizador o sea ADMIN
        if (!"ADMIN".equals(userRole) && !event.getOrganizadorId().equals(userId)) {
            throw new BadRequestException("No tienes permisos para actualizar este evento");
        }

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
            // Validar que la nueva fecha sea futura
            if (request.getFechaEvento().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("La fecha del evento debe ser futura");
            }
            event.setFechaEvento(request.getFechaEvento());
        }
        if (request.getCategoria() != null) {
            event.setCategoria(request.getCategoria());
        }
        if (request.getImagenUrl() != null) {
            event.setImagenUrl(request.getImagenUrl());
        }
        if (request.getActivo() != null) {
            event.setActivo(request.getActivo());
        }
        if (request.getEstado() != null) {
            try {
                Event.EstadoEvento nuevoEstado = Event.EstadoEvento.valueOf(request.getEstado());
                event.setEstado(nuevoEstado);
                // Sincronizar activo con estado
                event.setActivo(nuevoEstado == Event.EstadoEvento.ACTIVO);
            } catch (IllegalArgumentException e) {
                log.warn("Estado inválido recibido: {}", request.getEstado());
                // Ignorar estado inválido
            }
        }

        event.setFechaActualizacion(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        log.info("Evento actualizado: {}", updatedEvent.getId());

        return EventDto.fromEntity(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id, Long userId, String userRole) {
        log.info("Cancelando evento con ID: {} por usuario: {}", id, userId);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));

        // Solo ADMIN puede cambiar el estado de cualquier evento
        if ("ADMIN".equals(userRole)) {
            // Admin puede cancelar cualquier evento
            event.setEstado(Event.EstadoEvento.CANCELADO);
            event.setActivo(false);
            event.setFechaActualizacion(LocalDateTime.now());
            eventRepository.save(event);
            log.info("Evento cancelado por ADMIN: {}", id);
        } else {
            // Usuarios normales solo pueden cancelar sus propios eventos
            if (!event.getOrganizadorId().equals(userId)) {
                throw new BadRequestException("No tienes permisos para cancelar este evento");
            }
            // Marcar como CANCELADO
            event.setEstado(Event.EstadoEvento.CANCELADO);
            event.setActivo(false);
            event.setFechaActualizacion(LocalDateTime.now());
            eventRepository.save(event);
            log.info("Evento cancelado por organizador: {}", id);
        }
    }

    @Transactional
    public void finalizarEvento(Long id) {
        log.info("Finalizando evento con ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
        
        event.setEstado(Event.EstadoEvento.FINALIZADO);
        event.setActivo(false);
        event.setFechaActualizacion(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Evento finalizado: {}", id);
    }
}
