package com.example.ticketservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewaySecretInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GatewaySecretInterceptor.class);

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                        ClientHttpRequestExecution execution) throws IOException {
        logger.info("🔑 Agregando X-Gateway-Secret header a request: {} {}", 
            request.getMethod(), request.getURI());
        request.getHeaders().set("X-Gateway-Secret", gatewaySecret);
        return execution.execute(request, body);
    }
}
