package com.example.coresto.base;

import com.example.coresto.config.TestConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Global base class for Spring Boot Integration Tests.
 *
 * <p>Features:
 * <ul>
 *   <li>Loads the Spring ApplicationContext with {@link SpringBootTest @SpringBootTest}.</li>
 *   <li>Configures MockMvc via {@link AutoConfigureMockMvc @AutoConfigureMockMvc}.</li>
 *   <li>Activates the {@code "test"} profile (using in-memory H2 database).</li>
 *   <li>Imports {@link TestConfig} for automatic mock beans (Keycloak Admin client, JwtDecoder).</li>
 *   <li>Provides fluent HTTP request execution methods with built-in JSON and Security post-processors.</li>
 *   <li>Provides response deserialization helpers.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * class OrganizationControllerIntegrationTest extends BaseIntegrationTest {
 *
 *     @Test
 *     void testCreateOrg() throws Exception {
 *         CreateOrganizationRequest req = new CreateOrganizationRequest("My Restaurant", "my-rest", null);
 *
 *         performPost("/api/organizations", req, adminJwt())
 *                 .andExpect(status().isCreated())
 *                 .andExpect(jsonPath("$.name").value("My Restaurant"));
 *     }
 * }
 * }</pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired(required = false)
    protected ObjectMapper objectMapper = createDefaultObjectMapper();

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Autowired
    protected Keycloak keycloakAdmin;

    @Autowired
    protected org.springframework.web.client.RestTemplate restTemplateBean;

    // ──────────────────────────────────────────────────────────────────
    //  HTTP Request Helpers
    // ──────────────────────────────────────────────────────────────────

    /**
     * Executes a GET request with optional request post-processors (e.g. JWT auth).
     */
    protected ResultActions performGet(String uri, RequestPostProcessor... postProcessors) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON);
        applyPostProcessors(builder, postProcessors);
        return mockMvc.perform(builder);
    }

    /**
     * Executes a POST request with a JSON request body and optional request post-processors.
     */
    protected ResultActions performPost(String uri, Object body, RequestPostProcessor... postProcessors) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (body != null) {
            builder.content(toJson(body));
        }

        applyPostProcessors(builder, postProcessors);
        return mockMvc.perform(builder);
    }

    /**
     * Executes a PUT request with a JSON request body and optional request post-processors.
     */
    protected ResultActions performPut(String uri, Object body, RequestPostProcessor... postProcessors) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (body != null) {
            builder.content(toJson(body));
        }

        applyPostProcessors(builder, postProcessors);
        return mockMvc.perform(builder);
    }

    /**
     * Executes a PATCH request with a JSON request body and optional request post-processors.
     */
    protected ResultActions performPatch(String uri, Object body, RequestPostProcessor... postProcessors) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (body != null) {
            builder.content(toJson(body));
        }

        applyPostProcessors(builder, postProcessors);
        return mockMvc.perform(builder);
    }

    /**
     * Executes a DELETE request with optional request post-processors.
     */
    protected ResultActions performDelete(String uri, RequestPostProcessor... postProcessors) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete(uri)
                .accept(MediaType.APPLICATION_JSON);
        applyPostProcessors(builder, postProcessors);
        return mockMvc.perform(builder);
    }

    private void applyPostProcessors(MockHttpServletRequestBuilder builder, RequestPostProcessor... postProcessors) {
        if (postProcessors != null) {
            for (RequestPostProcessor postProcessor : postProcessors) {
                if (postProcessor != null) {
                    builder.with(postProcessor);
                }
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  Security / JWT Shortcuts
    // ──────────────────────────────────────────────────────────────────

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_ADMIN}.
     */
    protected RequestPostProcessor adminJwt() {
        return TestSecurityUtils.withAdminJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_OWNER}.
     */
    protected RequestPostProcessor ownerJwt() {
        return TestSecurityUtils.withOwnerJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_SUPER_MANAGER}.
     */
    protected RequestPostProcessor superManagerJwt() {
        return TestSecurityUtils.withSuperManagerJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_BRANCH_MANAGER}.
     */
    protected RequestPostProcessor branchManagerJwt() {
        return TestSecurityUtils.withBranchManagerJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with the 'branch-manger' alias role.
     */
    protected RequestPostProcessor branchMangerTypoJwt() {
        return TestSecurityUtils.withBranchMangerTypoJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_WORKER}.
     */
    protected RequestPostProcessor workerJwt() {
        return TestSecurityUtils.withWorkerJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with {@code ROLE_USER}.
     */
    protected RequestPostProcessor userJwt() {
        return TestSecurityUtils.withUserJwt();
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with custom realm roles.
     */
    protected RequestPostProcessor jwtWithRoles(String... roles) {
        return TestSecurityUtils.withJwtRoles(roles);
    }

    /**
     * Returns a MockMvc RequestPostProcessor providing a Keycloak JWT with custom username and roles.
     */
    protected RequestPostProcessor jwtWithUser(String username, String... roles) {
        return TestSecurityUtils.withJwtUser(username, roles);
    }

    // ──────────────────────────────────────────────────────────────────
    //  JSON Serialization & Response Parsing
    // ──────────────────────────────────────────────────────────────────

    /**
     * Serializes an object to JSON string.
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON: " + object, e);
        }
    }

    /**
     * Deserializes a MockMvc response into the target class.
     */
    protected <T> T parseResponse(MvcResult result, Class<T> clazz) {
        try {
            String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            return objectMapper.readValue(content, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response body to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a MockMvc response into a generic target type (e.g. {@code List<OrganizationResponse>}).
     */
    protected <T> T parseResponse(MvcResult result, TypeReference<T> typeReference) {
        try {
            String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            return objectMapper.readValue(content, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response body to generic type", e);
        }
    }
}
