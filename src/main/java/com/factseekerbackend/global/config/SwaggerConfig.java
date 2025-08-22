package com.factseekerbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition(
    info = @io.swagger.v3.oas.annotations.info.Info(
        title = "Fact-Seeker API Documentation", 
        description = "팩트시커 서비스의 API 명세서입니다.",
        version = "v1.0.0"
    ),
    servers = {
        @Server(url = "https://prod.fact-seeker.com", description = "배포 서버"),
        @Server(url = "http://localhost:8080", description = "로컬 개발 서버")
    })
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springOpenApi() {
        return new OpenAPI()
                .components(securityComponents())
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("AccessToken"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(
                        "AccessToken",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}
