package com.example.coresto.base;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Security test utilities for generating Keycloak-compatible JWT authentication tokens in MockMvc tests.
 *
 * <p>Constructs JWT tokens containing the {@code realm_access.roles} claim and maps them
 * to Spring Security {@code ROLE_*} granted authorities matching the application's converter rules.
 */
public final class TestSecurityUtils {

    private TestSecurityUtils() {
        // utility class
    }

    /**
     * Creates an authenticated JWT with default subject and no roles.
     */
    public static JwtRequestPostProcessor withJwt() {
        return withJwtRoles();
    }

    /**
     * Creates an authenticated JWT with the {@code ADMIN} realm role (mapped to {@code ROLE_ADMIN}).
     */
    public static JwtRequestPostProcessor withAdminJwt() {
        return withJwtRoles("ADMIN");
    }

    /**
     * Creates an authenticated JWT with the {@code owner} realm role (mapped to {@code ROLE_OWNER}).
     */
    public static JwtRequestPostProcessor withOwnerJwt() {
        return withJwtRoles("owner");
    }

    /**
     * Creates an authenticated JWT with the {@code super-manager} realm role (mapped to {@code ROLE_SUPER_MANAGER}).
     */
    public static JwtRequestPostProcessor withSuperManagerJwt() {
        return withJwtRoles("super-manager");
    }

    /**
     * Creates an authenticated JWT with the {@code branch-manager} realm role (mapped to {@code ROLE_BRANCH_MANAGER}).
     */
    public static JwtRequestPostProcessor withBranchManagerJwt() {
        return withJwtRoles("branch-manager");
    }

    /**
     * Creates an authenticated JWT with the {@code branch-manger} alias realm role.
     */
    public static JwtRequestPostProcessor withBranchMangerTypoJwt() {
        return withJwtRoles("branch-manger");
    }

    /**
     * Creates an authenticated JWT with the {@code worker} realm role (mapped to {@code ROLE_WORKER}).
     */
    public static JwtRequestPostProcessor withWorkerJwt() {
        return withJwtRoles("worker");
    }

    /**
     * Creates an authenticated JWT with the {@code USER} realm role (mapped to {@code ROLE_USER}).
     */
    public static JwtRequestPostProcessor withUserJwt() {
        return withJwtRoles("USER");
    }

    /**
     * Creates an authenticated JWT with the specified Keycloak realm roles.
     *
     * @param roles role names without the {@code ROLE_} prefix (e.g., "ADMIN", "MANAGER")
     */
    public static JwtRequestPostProcessor withJwtRoles(String... roles) {
        return withJwtUser("test-user", roles);
    }

    /**
     * Creates an authenticated JWT with a custom username and realm roles.
     *
     * @param username username / preferred_username
     * @param roles    role names without the {@code ROLE_} prefix
     */
    public static JwtRequestPostProcessor withJwtUser(String username, String... roles) {
        List<String> roleList = roles != null ? Arrays.asList(roles) : Collections.emptyList();
        Map<String, Object> realmAccess = Collections.singletonMap("roles", roleList);
        List<GrantedAuthority> authorities = mapRolesToAuthorities(roleList);

        return jwt()
                .authorities(authorities)
                .jwt(builder -> builder
                        .subject(UUID.randomUUID().toString())
                        .claim("preferred_username", username)
                        .claim("email", username + "@example.com")
                        .claim("realm_access", realmAccess)
                );
    }

    /**
     * Creates a fully customized JWT with subject, username, email, realm roles, and additional claims.
     */
    public static JwtRequestPostProcessor withCustomJwt(
            String subject,
            String username,
            String email,
            List<String> roles,
            Map<String, Object> extraClaims
    ) {
        List<GrantedAuthority> authorities = mapRolesToAuthorities(roles);

        return jwt()
                .authorities(authorities)
                .jwt(builder -> {
                    builder.subject(subject != null ? subject : UUID.randomUUID().toString())
                            .claim("preferred_username", username != null ? username : "custom-user")
                            .claim("email", email != null ? email : "custom-user@example.com")
                            .claim("realm_access", Collections.singletonMap("roles", roles != null ? roles : Collections.emptyList()));

                    if (extraClaims != null) {
                        extraClaims.forEach(builder::claim);
                    }
                });
    }

    private static List<GrantedAuthority> mapRolesToAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }
            String normalized = role.trim().replace("-", "_").toUpperCase();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
            if ("BRANCH_MANGER".equals(normalized)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_BRANCH_MANAGER"));
            } else if ("BRANCH_MANAGER".equals(normalized)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_BRANCH_MANGER"));
            }
        }
        return authorities;
    }
}
