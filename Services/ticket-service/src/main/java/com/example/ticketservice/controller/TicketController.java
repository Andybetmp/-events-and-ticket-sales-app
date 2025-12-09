package com.example.ticketservice.controller;

import com.example.ticketservice.dto.CreateTicketRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "API de gestión de tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Crear ticket", description = "Crea un nuevo ticket de compra")
    public ResponseEntity<TicketResponse> crearTicket(@RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.crearTicket(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Obtener ticket por ID", description = "Obtiene un ticket específico por su ID (solo propietario o ADMIN)")
    public ResponseEntity<TicketResponse> obtenerTicket(
            @PathVariable String ticketId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        TicketResponse response = ticketService.obtenerTicketPorIdConValidacion(ticketId, userId, userRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{usuarioId}")
    @Operation(summary = "Obtener tickets por usuario", description = "Obtiene todos los tickets de un usuario (solo propio usuario o ADMIN)")
    public ResponseEntity<List<TicketResponse>> obtenerTicketsPorUsuario(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        List<TicketResponse> tickets = ticketService.obtenerTicketsPorUsuarioConValidacion(usuarioId, requestUserId, userRole);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tickets", description = "Obtiene todos los tickets del sistema (solo ADMIN)")
    public ResponseEntity<List<TicketResponse>> obtenerTodosLosTickets(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        List<TicketResponse> tickets = ticketService.obtenerTodosLosTicketsConValidacion(userRole);
        return ResponseEntity.ok(tickets);
    }
}
