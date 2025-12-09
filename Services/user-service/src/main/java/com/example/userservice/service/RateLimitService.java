package com.example.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Rate Limiting para prevenir spam y ataques de fuerza bruta.
 * 
 * Implementa una estrategia de "sliding window" en memoria para:
 * - Password reset: Máximo 3 intentos por email cada 15 minutos
 * 
 * Características:
 * - Thread-safe con ConcurrentHashMap
 * - Limpieza automática de entradas expiradas
 * - Configuración flexible por tipo de operación
 */
@Service
@Slf4j
public class RateLimitService {

    // Configuración de rate limiting
    private static final int PASSWORD_RESET_MAX_ATTEMPTS = 3;
    private static final long PASSWORD_RESET_WINDOW_MINUTES = 15;
    
    // Almacenamiento en memoria de intentos por email
    private final Map<String, AttemptRecord> passwordResetAttempts = new ConcurrentHashMap<>();

    /**
     * Verifica si un email puede solicitar password reset.
     * @param email 
     * @return true si está permitido, false si excedió el límite
     */
    public boolean canRequestPasswordReset(String email) {
        String key = "pwd_reset:" + email.toLowerCase();
        AttemptRecord record = passwordResetAttempts.get(key);
        
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(PASSWORD_RESET_WINDOW_MINUTES * 60);
        
        // Si no hay registro previo, está permitido
        if (record == null) {
            return true;
        }
        
        // Si el último intento fue fuera de la ventana, limpiar y permitir
        if (record.firstAttempt.isBefore(windowStart)) {
            passwordResetAttempts.remove(key);
            return true;
        }
        
        // Si está dentro de la ventana, verificar cantidad de intentos
        boolean allowed = record.count < PASSWORD_RESET_MAX_ATTEMPTS;
        
        if (!allowed) {
            long minutesLeft = PASSWORD_RESET_WINDOW_MINUTES - 
                ((now.getEpochSecond() - record.firstAttempt.getEpochSecond()) / 60);
            log.warn("Rate limit excedido para password reset: {} | Intentos: {} | Esperar {} minutos", 
                email, record.count, minutesLeft);
        }
        
        return allowed;
    }

    /**
     * Registra un intento de password reset.
     * @param email Email del usuario
     */
    public void recordPasswordResetAttempt(String email) {
        String key = "pwd_reset:" + email.toLowerCase();
        Instant now = Instant.now();
        
        passwordResetAttempts.compute(key, (k, existing) -> {
            if (existing == null) {
                // Primer intento
                return new AttemptRecord(now, 1);
            } else {
                // Incrementar contador
                existing.count++;
                existing.lastAttempt = now;
                return existing;
            }
        });
        
        log.debug("Password reset attempt registrado para {}: {} intentos", email, 
            passwordResetAttempts.get(key).count);
    }

    /**
     * Obtiene el tiempo restante de espera en minutos.
     * @param email Email del usuario
     * @return Minutos restantes, o 0 si no hay restricción
     */
    public long getRemainingWaitMinutes(String email) {
        String key = "pwd_reset:" + email.toLowerCase();
        AttemptRecord record = passwordResetAttempts.get(key);
        
        if (record == null) {
            return 0;
        }
        
        Instant now = Instant.now();
        long elapsedMinutes = (now.getEpochSecond() - record.firstAttempt.getEpochSecond()) / 60;
        long remaining = PASSWORD_RESET_WINDOW_MINUTES - elapsedMinutes;
        
        return Math.max(0, remaining);
    }

    /**
     * Limpia registros expirados (llamar periódicamente con @Scheduled).
     */
    public void cleanupExpiredRecords() {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(PASSWORD_RESET_WINDOW_MINUTES * 60);
        
        passwordResetAttempts.entrySet().removeIf(entry -> 
            entry.getValue().firstAttempt.isBefore(windowStart)
        );
        
        log.info("Rate limit cleanup: {} registros activos", passwordResetAttempts.size());
    }

    /**
     * Registro de intentos con timestamp.
     */
    private static class AttemptRecord {
        Instant firstAttempt;
        Instant lastAttempt;
        int count;
        
        AttemptRecord(Instant firstAttempt, int count) {
            this.firstAttempt = firstAttempt;
            this.lastAttempt = firstAttempt;
            this.count = count;
        }
    }
}
