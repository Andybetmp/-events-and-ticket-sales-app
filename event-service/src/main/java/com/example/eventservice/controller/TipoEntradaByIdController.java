package com.example.eventservice.controller;

import com.example.eventservice.dto.TipoEntradaDto;
import com.example.eventservice.dto.UpdateTipoEntradaRequest;
import com.example.eventservice.service.TipoEntradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-entrada")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tipos de Entrada", description = "Operaciones sobre tipos de entrada específicos")
public class TipoEntradaByIdController {

    private final TipoEntradaService tipoEntradaService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de entrada", description = "Obtiene un tipo de entrada por su ID")
    public ResponseEntity<TipoEntradaDto> getTipoEntrada(@PathVariable Long id) {
        log.info("GET /api/tipos-entrada/{}", id);
        TipoEntradaDto tipoEntrada = tipoEntradaService.getTipoEntradaById(id);
        return ResponseEntity.ok(tipoEntrada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de entrada", description = "Actualiza un tipo de entrada existente")
    public ResponseEntity<TipoEntradaDto> updateTipoEntrada(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTipoEntradaRequest request) {
        log.info("PUT /api/tipos-entrada/{}", id);
        TipoEntradaDto tipoEntrada = tipoEntradaService.updateTipoEntrada(id, request);
        return ResponseEntity.ok(tipoEntrada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de entrada", description = "Elimina un tipo de entrada (solo si no tiene ventas)")
    public ResponseEntity<Void> deleteTipoEntrada(@PathVariable Long id) {
        log.info("DELETE /api/tipos-entrada/{}", id);
        tipoEntradaService.deleteTipoEntrada(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/disminuir")
    @Operation(summary = "Disminuir cantidad", description = "Disminuye la cantidad disponible (uso interno)")
    public ResponseEntity<Void> decreaseCantidad(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        log.info("PUT /api/tipos-entrada/{}/disminuir - cantidad: {}", id, cantidad);
        tipoEntradaService.decreaseCantidad(id, cantidad);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/incrementar")
    @Operation(summary = "Incrementar cantidad", description = "Incrementa la cantidad disponible - Compensación/Rollback")
    public ResponseEntity<Void> increaseCantidad(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        log.info("PUT /api/tipos-entrada/{}/incrementar - cantidad: {} (ROLLBACK)", id, cantidad);
        tipoEntradaService.increaseCantidad(id, cantidad);
        return ResponseEntity.ok().build();
    }
}
