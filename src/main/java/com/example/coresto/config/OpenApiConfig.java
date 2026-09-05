package com.example.coresto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI corestoOpenAPI() {
        // Keycloak OpenID Connect endpoints
        String authUrl = issuerUri + "/protocol/openid-connect/auth";
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";

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
                ))
                // OAuth2 security scheme — Authorization Code flow with Keycloak
                .components(new Components()
                        .addSecuritySchemes("keycloak-oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Keycloak OAuth2 Authorization Code Flow")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authUrl)
                                                .tokenUrl(tokenUrl)
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "User profile")
                                                )
                                        )
                                )
                        )
                )
                // Apply security globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList("keycloak-oauth2",
                        List.of("openid", "profile")));
    }
}