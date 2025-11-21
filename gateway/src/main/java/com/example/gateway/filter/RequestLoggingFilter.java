package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Generar Request ID único
        String requestId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        // Agregar headers de trazabilidad a la petición
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .header("X-Timestamp", timestamp)
                .build();

        // Log de entrada
        log.info("[{}] → {} {} from {} | User-Agent: {}",
                requestId,
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress(),
                request.getHeaders().getFirst("User-Agent"));

        // Continuar con la petición modificada y loguear respuesta
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    log.info("[{}] ← {} {} ({}ms)",
                            requestId,
                            exchange.getResponse().getStatusCode(),
                            request.getPath(),
                            duration);

                    // Advertencia si la latencia es alta
                    if (duration > 1000) {
                        log.warn("[{}] ⚠️ Alta latencia detectada: {}ms", requestId, duration);
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -100; // Ejecutar primero (antes que otros filtros)
    }
}
