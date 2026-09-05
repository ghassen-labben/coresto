package com.example.coresto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI corestoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coresto API")
                        .version("v1")
                        .description("""
                                API for restaurants, digital menus, QR codes,
                                table orders, and payment configuration.
                                """)
                        .contact(new Contact()
                                .name("Coresto")
                                .email("support@coresto.tn"))
                        .license(new License()
                                .name("Proprietary")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local environment"),
                        new Server()
                                .url("https://api.coresto.tn")
                                .description("Production environment")
                ));
    }
}