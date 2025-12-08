package com.example.ticketservice.controller;

import com.example.ticketservice.dto.CreateReservaRequest;
import com.example.ticketservice.dto.ReservaDto;
import com.example.ticketservice.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reservas", description = "API de gestión de reservas temporales")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/crear")
    @Operation(summary = "Crear reserva temporal", description = "Crea una reserva temporal y decrementa el stock por 10 minutos")
    public ResponseEntity<ReservaDto> crearReserva(@Valid @RequestBody CreateReservaRequest request) {
        ReservaDto response = reservaService.crearReserva(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reservaId}/confirmar")
    @Operation(summary = "Confirmar reserva", description = "Confirma la reserva después de un pago exitoso")
    public ResponseEntity<ReservaDto> confirmarReserva(@PathVariable Long reservaId) {
        ReservaDto response = reservaService.confirmarReserva(reservaId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reservaId}/liberar")
    @Operation(summary = "Liberar reserva", description = "Libera la reserva y restaura el stock (pago fallido o cancelación)")
    public ResponseEntity<ReservaDto> liberarReserva(@PathVariable Long reservaId) {
        ReservaDto response = reservaService.liberarReserva(reservaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{usuarioId}/activas")
    @Operation(summary = "Obtener reservas activas", description = "Obtiene todas las reservas activas de un usuario")
    public ResponseEntity<List<ReservaDto>> getReservasActivas(@PathVariable Long usuarioId) {
        List<ReservaDto> reservas = reservaService.getReservasActivas(usuarioId);
        return ResponseEntity.ok(reservas);
    }
}
