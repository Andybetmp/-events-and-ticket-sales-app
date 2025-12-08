package com.example.gateway.filter;

import com.example.gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro de autenticación JWT para Spring Cloud Gateway.
 * 
 * RESPONSABILIDADES:
 * 1. Extraer y validar el token JWT del header Authorization
 * 2. Enriquecer la petición con datos del usuario (X-User-Email, X-User-ID)
 * 3. Rechazar peticiones con tokens inválidos o expirados
 * 
 * FLUJO:
 * Cliente → Gateway (este filtro) → Validación JWT → Servicios internos
 * 
 * PATRONES IMPLEMENTADOS:
 * - Request Enrichment: Añade headers adicionales basados en el token
 * - Security Gateway: Punto único de validación de autenticación
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // PASO 1: Extraer token del header Authorization (formato: "Bearer <token>")
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Token de autorización no encontrado", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Formato de token inválido. Use: Bearer <token>", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Remover "Bearer "

            // PASO 2: Validar integridad y expiración del token JWT
            if (!jwtService.isTokenValid(token)) {
                return onError(exchange, "Token inválido o expirado", HttpStatus.UNAUTHORIZED);
            }

            // PASO 3: Extraer datos del token (REQUEST ENRICHMENT PATTERN)
            // Los servicios internos recibirán estos headers y NO necesitan validar JWT
            String email = jwtService.extractEmail(token);
            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);

            // PASO 4: Añadir headers enriquecidos para los servicios internos
            // Los servicios pueden confiar en estos headers porque Gateway ya validó el JWT
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-User-ID", userId != null ? userId : "unknown")
                    .header("X-User-Role", role)
                    .build();

            // PASO 5: Continuar con la petición enriquecida hacia el servicio destino
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    /**
     * Método helper para devolver errores de autenticación al cliente.
     * Retorna una respuesta JSON con el mensaje de error y código HTTP apropiado.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format("{\"error\":\"%s\",\"status\":%d}", message, status.value());
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties si se necesitan en el futuro
    }
}
