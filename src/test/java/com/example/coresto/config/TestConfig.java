package com.example.coresto.config;

import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.web.client.RestTemplate;

/**
 * Test configuration providing mock beans for external services (Keycloak Admin client,
 * JwtDecoder, RestTemplate) so integration tests can run without live external services.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @Primary
    public Keycloak mockKeycloakAdmin() {
        return Mockito.mock(Keycloak.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    @Primary
    public JwtDecoder mockJwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    public RestTemplate mockRestTemplate() {
        return Mockito.mock(RestTemplate.class);
    }
}
