package com.example.ticketservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private GatewaySecretInterceptor gatewaySecretInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))  // 10 segundos para conectar
                .setReadTimeout(Duration.ofSeconds(30))     // 30 segundos para el procesamiento del pago
                .build();
        
        // Add interceptor to include X-Gateway-Secret header
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(gatewaySecretInterceptor);
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
}
