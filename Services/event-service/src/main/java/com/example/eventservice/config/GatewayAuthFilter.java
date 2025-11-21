package com.example.eventservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "gateway.validation.enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    private static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/health",
            "/api-docs",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Permitir acceso público a ciertas rutas
        if (isPublicPath(path)) {
            log.debug("Ruta pública permitida: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Validar que la petición viene del gateway
        String gatewaySecretHeader = request.getHeader(GATEWAY_SECRET_HEADER);
        
        if (gatewaySecretHeader == null || !gatewaySecretHeader.equals(gatewaySecret)) {
            log.warn("Acceso no autorizado a ruta protegida: {}. Gateway secret inválido o ausente", path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. Las peticiones deben pasar por el Gateway\"}");
            return;
        }

        log.debug("Petición autorizada desde Gateway para ruta: {}", path);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
