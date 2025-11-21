package com.example.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorStandardizationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ErrorStandardizationFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
                    
                    log.error("[{}] Error en Gateway: {}", requestId, throwable.getMessage(), throwable);

                    return handleError(exchange, throwable, requestId);
                });
    }

    private Mono<Void> handleError(ServerWebExchange exchange, Throwable throwable, String requestId) {
        ServerHttpResponse response = exchange.getResponse();

        // Determinar el status code apropiado
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorType = "Internal Server Error";

        if (throwable instanceof org.springframework.web.server.ResponseStatusException) {
            org.springframework.web.server.ResponseStatusException rse = 
                (org.springframework.web.server.ResponseStatusException) throwable;
            status = (HttpStatus) rse.getStatusCode();
            errorType = status.getReasonPhrase();
        }

        // Crear respuesta de error estandarizada
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", errorType);
        errorResponse.put("message", throwable.getMessage() != null ? throwable.getMessage() : "Error inesperado");
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("requestId", requestId != null ? requestId : "N/A");

        // Configurar respuesta
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error al serializar respuesta de error", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -50; // Ejecutar después del RequestLoggingFilter pero antes de otros
    }
}
