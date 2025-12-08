package com.example.ticketservice.service;

import com.example.ticketservice.dto.CreateReservaRequest;
import com.example.ticketservice.dto.ReservaDto;
import com.example.ticketservice.exception.InsufficientStockException;
import com.example.ticketservice.exception.ReservaExpiredException;
import com.example.ticketservice.exception.ReservaNotFoundException;
import com.example.ticketservice.model.Reserva;
import com.example.ticketservice.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservaService {
    
    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
    private static final long EXPIRACION_MINUTOS = 10; // 10 minutos para completar el pago
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${event.service.url:http://localhost:8082}")
    private String eventServiceUrl;
    
    @Value("${gateway.secret}")
    private String gatewaySecret;
    
    /**
     * Crear una reserva temporal (decrementa el stock en event-service)
     */
    @Transactional
    public ReservaDto crearReserva(CreateReservaRequest request) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ CREANDO RESERVA TEMPORAL                                ║");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ Tipo Entrada ID: {}", request.getTipoEntradaId());
        log.info("║ Usuario ID:      {}", request.getUsuarioId());
        log.info("║ Cantidad:        {}", request.getCantidad());
        
        try {
            // Decrementar stock en event-service
            String url = eventServiceUrl + "/api/tipos-entrada/" + request.getTipoEntradaId() + "/disminuir?cantidad=" + request.getCantidad();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Gateway-Secret", gatewaySecret);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("║ Stock decrementado en event-service                    ║");
            
        } catch (Exception e) {
            log.error("║ RESULTADO:       ❌ ERROR - {}                  ║", e.getMessage());
            log.info("═══════════════════════════════════════════════════════════");
            throw new InsufficientStockException("No hay stock disponible para este tipo de entrada");
        }
        
        // Crear reserva con expiración de 10 minutos
        Instant expiracion = Instant.now().plusSeconds(EXPIRACION_MINUTOS * 60);
        Reserva reserva = new Reserva(
            request.getTipoEntradaId(),
            request.getUsuarioId(),
            request.getCantidad(),
            expiracion
        );
        
        reserva = reservaRepository.save(reserva);
        
        log.info("║ Reserva ID:      {}", reserva.getId());
        log.info("║ Expira en:       {} minutos", EXPIRACION_MINUTOS);
        log.info("║ RESULTADO:       ✅ RESERVA CREADA EXITOSAMENTE         ║");
        log.info("═══════════════════════════════════════════════════════════");
        
        return ReservaDto.fromEntity(reserva);
    }
    
    /**
     * Confirmar reserva (pago exitoso)
     */
    @Transactional
    public ReservaDto confirmarReserva(Long reservaId) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ CONFIRMANDO RESERVA (Pago exitoso)                      ║");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ Reserva ID: {}", reservaId);
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada con ID: " + reservaId));
        
        if (reserva.getEstado() != Reserva.EstadoReserva.ACTIVA) {
            log.error("║ RESULTADO: ❌ Reserva no está activa (estado: {})", reserva.getEstado());
            log.info("═══════════════════════════════════════════════════════════");
            throw new IllegalArgumentException("La reserva no está activa, estado actual: " + reserva.getEstado());
        }
        
        // Verificar si no expiró
        if (Instant.now().isAfter(reserva.getFechaExpiracion())) {
            log.error("║ RESULTADO: ❌ Reserva expirada");
            log.info("═══════════════════════════════════════════════════════════");
            throw new ReservaExpiredException("La reserva ha expirado el " + reserva.getFechaExpiracion());
        }
        
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADA);
        reserva = reservaRepository.save(reserva);
        
        log.info("║ RESULTADO: ✅ RESERVA CONFIRMADA - Stock ya decrementado");
        log.info("═══════════════════════════════════════════════════════════");
        
        return ReservaDto.fromEntity(reserva);
    }
    
    /**
     * Liberar reserva (pago fallido o cancelación manual)
     */
    @Transactional
    public ReservaDto liberarReserva(Long reservaId) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ LIBERANDO RESERVA (Restaurando stock)                   ║");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("║ Reserva ID: {}", reservaId);
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada con ID: " + reservaId));
        
        if (reserva.getEstado() != Reserva.EstadoReserva.ACTIVA) {
            log.warn("║ ADVERTENCIA: Reserva no está activa (estado: {})", reserva.getEstado());
            log.info("═══════════════════════════════════════════════════════════");
            return ReservaDto.fromEntity(reserva);
        }
        
        try {
            // Incrementar stock en event-service
            String url = eventServiceUrl + "/api/tipos-entrada/" + reserva.getTipoEntradaId() + "/incrementar?cantidad=" + reserva.getCantidad();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Gateway-Secret", gatewaySecret);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("║ Stock restaurado en event-service (+{})", reserva.getCantidad());
            
        } catch (Exception e) {
            log.error("║ ERROR restaurando stock: {}", e.getMessage());
            throw new RuntimeException("Error al restaurar stock: " + e.getMessage());
        }
        
        reserva.setEstado(Reserva.EstadoReserva.LIBERADA);
        reserva = reservaRepository.save(reserva);
        
        log.info("║ RESULTADO: ✅ RESERVA LIBERADA - Stock restaurado");
        log.info("═══════════════════════════════════════════════════════════");
        
        return ReservaDto.fromEntity(reserva);
    }
    
    /**
     * Obtener reservas activas de un usuario
     */
    public List<ReservaDto> getReservasActivas(Long usuarioId) {
        return reservaRepository.findByUsuarioIdAndEstado(usuarioId, Reserva.EstadoReserva.ACTIVA)
            .stream()
            .map(ReservaDto::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Job automático para liberar reservas expiradas
     * Se ejecuta cada 30 segundos
     */
    @Scheduled(fixedDelay = 30000) // 30 segundos
    @Transactional
    public void liberarReservasExpiradas() {
        List<Reserva> expiradas = reservaRepository.findReservasExpiradas(Instant.now());
        
        if (!expiradas.isEmpty()) {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("║ JOB: Liberando {} reservas expiradas", expiradas.size());
            log.info("═══════════════════════════════════════════════════════════");
            
            for (Reserva reserva : expiradas) {
                try {
                    liberarReserva(reserva.getId());
                } catch (Exception e) {
                    log.error("║ ERROR al liberar reserva {}: {}", reserva.getId(), e.getMessage());
                }
            }
        }
    }
}
