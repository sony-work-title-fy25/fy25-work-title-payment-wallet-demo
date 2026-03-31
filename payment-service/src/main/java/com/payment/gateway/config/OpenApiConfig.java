package com.payment.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unified Payment Gateway API")
                        .version("1.0.0")
                        .description("Unified Payment Gateway Service with Loyalty Points Integration. " +
                                "Handles payments, refunds, and payment method management.")
                        .contact(new Contact()
                                .name("GDFT Team")
                                .email("gdft-team@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
