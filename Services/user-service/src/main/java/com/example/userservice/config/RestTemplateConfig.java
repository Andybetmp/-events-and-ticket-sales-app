package com.example.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configuración de RestTemplate con interceptor para comunicación entre servicios.
 * Agrega automáticamente el header X-Gateway-Secret en todas las peticiones.
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {
    
    private final GatewaySecretInterceptor gatewaySecretInterceptor;
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(gatewaySecretInterceptor));
        return restTemplate;
    }
}
