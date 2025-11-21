package com.example.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Servicio de Usuarios",
                version = "1.0",
                description = "Servicio de autenticación y gestión de usuarios para el Sistema SOA de Venta de Entradas"
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Servidor local")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Autenticación JWT",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
