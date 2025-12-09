package com.example.ticketservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)
public class GatewayAuthFilter implements Filter {

    @Value("${gateway.secret}")
    private String gatewaySecret;
    
    @Value("${gateway.validation.enabled:false}")
    private boolean validationEnabled;

    private static final List<String> PUBLIC_PATHS = List.of(
        "/swagger-ui",
        "/v3/api-docs",
        "/api-docs",
        "/actuator"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String secret = httpRequest.getHeader("X-Gateway-Secret");

        // Permitir rutas públicas
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Si la validación está deshabilitada, permitir todas las peticiones
        if (!validationEnabled) {
            chain.doFilter(request, response);
            return;
        }

        // Validar que viene del Gateway
        if (!gatewaySecret.equals(secret)) {
            sendForbiddenResponse(httpResponse,
                "Acceso denegado. Debes acceder a través del API Gateway en el puerto 8080. " +
                "URL correcta: http://localhost:8080" + path);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::contains);
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format("{\"error\":\"%s\",\"status\":403}", message)
        );
    }
}
