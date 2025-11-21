package com.example.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "API Gateway");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("port", 8080);
        return Mono.just(health);
    }
    
    @GetMapping("/")
    public Mono<Map<String, String>> root() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "SOA Ticketing System - API Gateway");
        info.put("version", "1.0.0");
        info.put("description", "Gateway para sistema de venta de entradas con arquitectura SOA");
        info.put("health", "/health");
        info.put("user-service", "http://localhost:8080/api/users/**");
        return Mono.just(info);
    }
}

