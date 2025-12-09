package com.example.userservice.config;

import com.example.userservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuración de tareas programadas para limpieza automática.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

    private final RateLimitService rateLimitService;

    /**
     * Limpia registros expirados de rate limiting cada hora.
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    public void cleanupRateLimitRecords() {
        log.info("🧹 Ejecutando limpieza programada de rate limit records...");
        rateLimitService.cleanupExpiredRecords();
    }
}
