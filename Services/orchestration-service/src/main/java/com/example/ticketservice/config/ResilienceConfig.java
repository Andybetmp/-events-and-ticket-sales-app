package com.example.ticketservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // 50% failure rate opens circuit
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before half-open
            .slidingWindowSize(10) // Last 10 calls to calculate failure rate
            .minimumNumberOfCalls(5) // Minimum 5 calls before calculating failure rate
            .permittedNumberOfCallsInHalfOpenState(3) // 3 test calls in half-open
            .build();
        
        return CircuitBreakerRegistry.of(config);
    }
}
