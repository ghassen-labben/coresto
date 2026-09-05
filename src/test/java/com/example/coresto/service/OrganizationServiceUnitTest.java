package com.example.coresto.service;

import com.example.coresto.base.BaseUnitTest;
import com.example.coresto.base.TestDataFactory;
import com.example.coresto.dto.PaginatedResponse;
import com.example.coresto.dto.organization.CreateOrganizationRequest;
import com.example.coresto.dto.organization.OrganizationResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.OrganizationsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrganizationServiceUnitTest extends BaseUnitTest {

    @Mock
    private Keycloak keycloakAdmin;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RealmResource realmResource;

    @Mock
    private OrganizationsResource organizationsResource;

    @Mock
    private Response response;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(organizationService, "targetRealm", "coresto");
        ReflectionTestUtils.setField(organizationService, "keycloakServerUrl", "http://localhost:8081");
    }

    @Test
    @DisplayName("createOrganization creates organization and extracts ID from location header")
    void testCreateOrganization_Success() {
        CreateOrganizationRequest request = TestDataFactory.createOrgRequest("Pizza Palace");

        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.create(any(OrganizationRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("http://localhost:8081/admin/realms/coresto/organizations/org-123");

        OrganizationResponse result = organizationService.createOrganization(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("org-123");
        assertThat(result.getName()).isEqualTo("Pizza Palace");
        assertThat(result.getAlias()).isEqualTo("pizza-palace");
        assertThat(result.isEnabled()).isTrue();

        // Verify JSON serialization utility from BaseUnitTest works
        String json = asJson(result);
        assertThat(json).contains("org-123");
    }

    @Test
    @DisplayName("createOrganization throws IllegalArgumentException when name is blank")
    void testCreateOrganization_BlankName_ThrowsException() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder().name("").build();

        assertThatThrownBy(() -> organizationService.createOrganization(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is required");
    }

    @Test
    @DisplayName("listOrganizations returns paginated response with metadata")
    void testListOrganizations_Success() {
        OrganizationRepresentation rep = new OrganizationRepresentation();
        rep.setId("org-456");
        rep.setName("Burger Queen");
        rep.setAlias("burger-queen");
        rep.setEnabled(true);

        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.list(0, 10)).thenReturn(List.of(rep));

        // Mock the count REST call
        when(restTemplate.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/count"),
                eq(HttpMethod.GET),
                any(),
                eq(Long.class)
        )).thenReturn(ResponseEntity.ok(25L));

        // Mock the token manager for auth headers
        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        PaginatedResponse<OrganizationResponse> result = organizationService.listOrganizations(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("org-456");
        assertThat(result.getContent().get(0).getName()).isEqualTo("Burger Queen");
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25L);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result.getNumberOfElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("listOrganizations uses defaults when first and max are null")
    void testListOrganizations_Defaults() {
        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.list(0, 20)).thenReturn(List.of());

        when(restTemplate.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/count"),
                eq(HttpMethod.GET),
                any(),
                eq(Long.class)
        )).thenReturn(ResponseEntity.ok(0L));

        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        PaginatedResponse<OrganizationResponse> result = organizationService.listOrganizations(null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }
}
