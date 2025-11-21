package com.example.eventservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Service API")
                        .description("API para gestión de eventos en el sistema de venta de entradas")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SOA Ticketing Team")
                                .email("support@ticketing.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
