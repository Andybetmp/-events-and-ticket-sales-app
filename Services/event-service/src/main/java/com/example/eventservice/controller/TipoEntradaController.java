package com.example.eventservice.controller;

import com.example.eventservice.dto.CreateTipoEntradaRequest;
import com.example.eventservice.dto.TipoEntradaDto;
import com.example.eventservice.dto.UpdateTipoEntradaRequest;
import com.example.eventservice.service.TipoEntradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{eventoId}/tipos-entrada")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tipos de Entrada", description = "Gestión de tipos de entrada para eventos")
public class TipoEntradaController {

    private final TipoEntradaService tipoEntradaService;

    @PostMapping
    @Operation(summary = "Crear tipo de entrada", description = "Crea un nuevo tipo de entrada para un evento")
    public ResponseEntity<TipoEntradaDto> createTipoEntrada(
            @PathVariable Long eventoId,
            @Valid @RequestBody CreateTipoEntradaRequest request) {
        log.info("POST /api/eventos/{}/tipos-entrada - Crear tipo de entrada: {}", eventoId, request.getNombre());
        TipoEntradaDto tipoEntrada = tipoEntradaService.createTipoEntrada(eventoId, request);
        return new ResponseEntity<>(tipoEntrada, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de entrada", description = "Obtiene todos los tipos de entrada de un evento")
    public ResponseEntity<List<TipoEntradaDto>> getTiposEntrada(
            @PathVariable Long eventoId,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        log.info("GET /api/eventos/{}/tipos-entrada - soloActivos: {}", eventoId, soloActivos);
        List<TipoEntradaDto> tiposEntrada = tipoEntradaService.getTiposEntradaByEvento(eventoId, soloActivos);
        return ResponseEntity.ok(tiposEntrada);
    }
}
