package com.example.coresto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public documentation & Swagger
                        .requestMatchers(
                                "/swagger",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Actuator health & info probes (for Kubernetes/Docker liveness and readiness)
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()

                        // Public customer/diner flow (browsing menus, guest orders)
                        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/orders").permitAll()

                        // ── Centralized Role-Based Access Control (RBAC) ──
                        // 1. Organization profile creation: exclusively OWNER or platform ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/organizations").hasAnyRole("OWNER", "ADMIN")

                        // 2. Listing organizations: ADMIN, OWNER, or SUPER_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/organizations").hasAnyRole("ADMIN", "OWNER", "SUPER_MANAGER")

                        // 3. Branch / Group creation within an organization: exclusively OWNER or platform ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/organizations/*/groups").hasAnyRole("OWNER", "ADMIN")

                        // 4. Listing branches / groups: ADMIN, OWNER, SUPER_MANAGER, or BRANCH_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/organizations/*/groups").hasAnyRole("ADMIN", "OWNER", "SUPER_MANAGER", "BRANCH_MANAGER", "BRANCH_MANGER")

                        // 5. Viewing single organization details: ADMIN, OWNER, SUPER_MANAGER, or BRANCH_MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/organizations/*").hasAnyRole("ADMIN", "OWNER", "SUPER_MANAGER", "BRANCH_MANAGER", "BRANCH_MANGER")

                        // Internal SaaS API (authenticated baseline)
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .build();
    }

    /**
     * Extracts Keycloak realm and client roles from the JWT
     * and maps them to Spring Security {@code ROLE_*} granted authorities.
     *
     * <p>This enables {@code @PreAuthorize("hasRole('OWNER')")} etc. in controllers.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Converter that reads roles from Keycloak JWT token ({@code realm_access.roles}
     * and {@code resource_access.*.roles}) and maps each role to a {@link SimpleGrantedAuthority}
     * with {@code ROLE_} prefix, normalizing hyphens to underscores (e.g. 'super-manager' -> 'ROLE_SUPER_MANAGER')
     * and supporting role aliases (e.g. 'branch-manger' <-> 'branch-manager').
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // 1. Extract from realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null) {
                    realmRoles.forEach(role -> addAuthoritiesForRole(authorities, role));
                }
            }

            // 2. Extract from resource_access.*.roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                for (Object clientObj : resourceAccess.values()) {
                    if (clientObj instanceof Map<?, ?> clientMap && clientMap.containsKey("roles")) {
                        List<String> clientRoles = (List<String>) clientMap.get("roles");
                        if (clientRoles != null) {
                            clientRoles.forEach(role -> addAuthoritiesForRole(authorities, role));
                        }
                    }
                }
            }

            return authorities;
        }

        private void addAuthoritiesForRole(Set<GrantedAuthority> authorities, String rawRole) {
            if (rawRole == null || rawRole.isBlank()) {
                return;
            }
            String normalized = rawRole.trim().replace("-", "_").toUpperCase();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));

            // Support alias for branch-manger / branch-manager
            if ("BRANCH_MANGER".equals(normalized)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_BRANCH_MANAGER"));
            } else if ("BRANCH_MANAGER".equals(normalized)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_BRANCH_MANGER"));
            }
        }
    }
}