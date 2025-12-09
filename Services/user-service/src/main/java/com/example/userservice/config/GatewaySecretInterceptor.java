package com.example.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Interceptor que agrega automáticamente el header X-Gateway-Secret
 * a todas las peticiones RestTemplate para comunicación entre servicios.
 */
@Component
@Slf4j
public class GatewaySecretInterceptor implements ClientHttpRequestInterceptor {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                        ClientHttpRequestExecution execution) throws IOException {
        log.debug("Agregando X-Gateway-Secret header a request: {} {}", 
            request.getMethod(), request.getURI());
        request.getHeaders().set("X-Gateway-Secret", gatewaySecret);
        return execution.execute(request, body);
    }
}
