package com.example.eventservice.controller;

import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.dto.EventDto;
import com.example.eventservice.dto.UpdateEventRequest;
import com.example.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "API para gestión de eventos")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Crear evento", description = "Crea un nuevo evento en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventDto event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Obtiene todos los eventos o filtra por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos obtenidos exitosamente")
    })
    public ResponseEntity<List<EventDto>> getAllEvents(
            @Parameter(description = "Filtrar solo eventos activos (por defecto true)")
            @RequestParam(required = false, defaultValue = "true") boolean onlyActive,
            @Parameter(description = "Filtrar solo eventos próximos")
            @RequestParam(required = false, defaultValue = "false") boolean upcoming,
            @Parameter(description = "Filtrar solo eventos con disponibilidad")
            @RequestParam(required = false, defaultValue = "false") boolean withAvailability) {
        
        List<EventDto> events;
        
        if (upcoming) {
            events = eventService.getUpcomingEvents();
        } else if (withAvailability) {
            events = eventService.getEventsWithAvailability();
        } else if (onlyActive) {
            events = eventService.getActiveEvents();
        } else {
            events = eventService.getAllEvents();
        }
        
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", description = "Obtiene los detalles de un evento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventDto> getEventById(
            @Parameter(description = "ID del evento")
            @PathVariable Long id) {
        EventDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evento", description = "Actualiza la información de un evento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este evento")
    })
    public ResponseEntity<EventDto> updateEvent(
            @Parameter(description = "ID del evento")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "USUARIO") String userRole) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        EventDto event = eventService.updateEvent(id, request, userId, userRole);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar evento", description = "Cancela un evento (usuarios) o desactiva (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento cancelado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para cancelar este evento")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID del evento")
            @PathVariable Long id,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "USUARIO") String userRole) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        eventService.deleteEvent(id, userId, userRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar evento", description = "Marca un evento como finalizado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento finalizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<Void> finalizarEvento(
            @Parameter(description = "ID del evento")
            @PathVariable Long id) {
        eventService.finalizarEvento(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "event-service"
        ));
    }
}
