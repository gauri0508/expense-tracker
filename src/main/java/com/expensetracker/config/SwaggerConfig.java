package com.expensetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Tracker API")
                        .description("""
                                A production-grade REST API for tracking expenses, managing budgets, and analyzing spending patterns.

                                ## Features
                                - User authentication with JWT
                                - Expense management (CRUD)
                                - Category management
                                - Budget tracking with alerts
                                - Analytics and reporting
                                - Multi-currency support
                                - Receipt file upload
                                - Email notifications

                                ## Authentication
                                Use the `/api/v1/auth/login` endpoint to obtain a JWT token, then include it in the Authorization header as `Bearer <token>`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Expense Tracker Team")
                                .email("support@expensetracker.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token")));
    }
}
