package com.example.coresto.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a Keycloak Admin Client bean using a service account
 * (client credentials grant) for programmatic management of
 * organizations, organization groups, users, and roles.
 *
 * <p>Requires a confidential client in the {@code master} realm
 * with "Service account roles" enabled and the {@code admin} role assigned.
 *
 * <p>Usage example in a service:
 * <pre>
 *   {@code @Autowired Keycloak keycloakAdmin;}
 *   keycloakAdmin.realm("coresto").organizations().create(orgRepresentation);
 * </pre>
 */
@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
