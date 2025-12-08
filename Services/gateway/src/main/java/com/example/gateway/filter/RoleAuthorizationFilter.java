package com.example.gateway.filter;

import com.example.gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Filtro de autorización basado en roles para Spring Cloud Gateway.
 * 
 * RESPONSABILIDADES:
 * 1. Validar que el usuario tenga uno de los roles permitidos para la ruta
 * 2. Rechazar peticiones de usuarios sin permisos suficientes
 * 
 * CONFIGURACIÓN:
 * En application.yml:
 *   filters:
 *     - RoleAuthorization=ADMIN,USUARIO
 * 
 * IMPORTANTE: Este filtro debe aplicarse DESPUÉS de JwtAuthenticationFilter
 */
@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    @Autowired
    private JwtService jwtService;

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // PASO 1: Extraer token del header (ya validado por JwtAuthenticationFilter)
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "No autenticado", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // PASO 2: Extraer rol del JWT
            String userRole = jwtService.extractRole(token);

            // PASO 3: Validar que el rol del usuario esté en la lista de roles permitidos
            if (!config.getRoles().contains(userRole)) {
                return onError(exchange, "Acceso denegado. Rol requerido: " + config.getRoles(), HttpStatus.FORBIDDEN);
            }

            // PASO 4: Usuario autorizado, continuar
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format("{\"error\":\"%s\",\"status\":%d}", message, status.value());
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
        private List<String> roles;

        public Config() {
            this.roles = Arrays.asList("USUARIO");
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
