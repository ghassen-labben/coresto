package com.example.coresto.controller;

import com.example.coresto.base.BaseIntegrationTest;
import com.example.coresto.base.TestDataFactory;
import com.example.coresto.dto.organization.CreateOrganizationGroupRequest;
import com.example.coresto.dto.organization.CreateOrganizationRequest;
import com.example.coresto.dto.organization.OrganizationResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.OrganizationsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizationControllerIntegrationTest extends BaseIntegrationTest {

    // ──────────────────────────────────────────────────────────────────
    //  List Organizations (ADMIN, OWNER, SUPER_MANAGER)
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/organizations returns 401 when request is unauthenticated")
    void testListOrganizations_Unauthenticated_ReturnsUnauthorized() throws Exception {
        performGet("/api/organizations")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/organizations returns 403 when authenticated as WORKER")
    void testListOrganizations_WorkerAuth_ReturnsForbidden() throws Exception {
        performGet("/api/organizations", workerJwt())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/organizations returns 200 when authenticated as OWNER")
    void testListOrganizations_OwnerAuth_ReturnsOk() throws Exception {
        mockKeycloakListAndCount();

        MvcResult result = performGet("/api/organizations", ownerJwt())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("org-999"))
                .andExpect(jsonPath("$.content[0].name").value("Taco House"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andReturn();
    }

    @Test
    @DisplayName("GET /api/organizations?first=0&max=10 returns 200 when authenticated as SUPER_MANAGER")
    void testListOrganizations_SuperManagerAuth_ReturnsOk() throws Exception {
        RealmResource realmResource = mock(RealmResource.class);
        OrganizationsResource organizationsResource = mock(OrganizationsResource.class);

        OrganizationRepresentation rep = new OrganizationRepresentation();
        rep.setId("org-999");
        rep.setName("Taco House");
        rep.setAlias("taco-house");
        rep.setEnabled(true);

        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.list(0, 10)).thenReturn(List.of(rep));

        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        when(restTemplateBean.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/count"),
                eq(HttpMethod.GET),
                any(),
                eq(Long.class)
        )).thenReturn(ResponseEntity.ok(25L));

        performGet("/api/organizations?first=0&max=10", superManagerJwt())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("org-999"))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.numberOfElements").value(1));
    }

    // ──────────────────────────────────────────────────────────────────
    //  Create Organization (exclusively OWNER or ADMIN)
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/organizations returns 201 when authenticated with Admin JWT")
    void testCreateOrganization_AdminAuth_ReturnsCreated() throws Exception {
        CreateOrganizationRequest request = TestDataFactory.createOrgRequest("Sushi Place");
        mockKeycloakCreateOrg("org-sushi-1");

        MvcResult result = performPost("/api/organizations", request, adminJwt())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("org-sushi-1"))
                .andExpect(jsonPath("$.name").value("Sushi Place"))
                .andReturn();

        OrganizationResponse createdOrg = parseResponse(result, OrganizationResponse.class);
        assertThat(createdOrg.getId()).isEqualTo("org-sushi-1");
        assertThat(createdOrg.getName()).isEqualTo("Sushi Place");
    }

    @Test
    @DisplayName("POST /api/organizations returns 201 when authenticated as OWNER")
    void testCreateOrganization_OwnerAuth_ReturnsCreated() throws Exception {
        CreateOrganizationRequest request = TestDataFactory.createOrgRequest("Pizza Hub");
        mockKeycloakCreateOrg("org-pizza-1");

        performPost("/api/organizations", request, ownerJwt())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("org-pizza-1"))
                .andExpect(jsonPath("$.name").value("Pizza Hub"));
    }

    @Test
    @DisplayName("POST /api/organizations returns 403 when authenticated as SUPER_MANAGER")
    void testCreateOrganization_SuperManagerAuth_ReturnsForbidden() throws Exception {
        CreateOrganizationRequest request = TestDataFactory.createOrgRequest("Burger Place");

        performPost("/api/organizations", request, superManagerJwt())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/organizations returns 403 when authenticated as WORKER")
    void testCreateOrganization_WorkerAuth_ReturnsForbidden() throws Exception {
        CreateOrganizationRequest request = TestDataFactory.createOrgRequest("Taco Bar");

        performPost("/api/organizations", request, workerJwt())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/organizations returns 400 when name is blank")
    void testCreateOrganization_BlankName_ReturnsBadRequest() throws Exception {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder().name("   ").build();

        performPost("/api/organizations", request, ownerJwt())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value("Organization name is required"));
    }

    // ──────────────────────────────────────────────────────────────────
    //  Create Branch Group (exclusively OWNER or ADMIN)
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/organizations/{orgId}/groups returns 201 when authenticated as OWNER")
    void testCreateBranchGroup_OwnerAuth_ReturnsCreated() throws Exception {
        CreateOrganizationGroupRequest request = TestDataFactory.createOrgGroupRequest("Downtown Branch");

        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "http://localhost:8081/admin/realms/coresto/organizations/org-123/groups/grp-456");
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(headers, HttpStatus.CREATED);

        when(restTemplateBean.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/org-123/groups"),
                eq(HttpMethod.POST),
                any(),
                eq(Void.class)
        )).thenReturn(responseEntity);

        performPost("/api/organizations/org-123/groups", request, ownerJwt())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("grp-456"))
                .andExpect(jsonPath("$.name").value("Downtown Branch"))
                .andExpect(jsonPath("$.organizationId").value("org-123"));
    }

    @Test
    @DisplayName("POST /api/organizations/{orgId}/groups returns 403 when authenticated as SUPER_MANAGER")
    void testCreateBranchGroup_SuperManagerAuth_ReturnsForbidden() throws Exception {
        CreateOrganizationGroupRequest request = TestDataFactory.createOrgGroupRequest("Uptown Branch");

        performPost("/api/organizations/org-123/groups", request, superManagerJwt())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/organizations/{orgId}/groups returns 403 when authenticated as WORKER")
    void testCreateBranchGroup_WorkerAuth_ReturnsForbidden() throws Exception {
        CreateOrganizationGroupRequest request = TestDataFactory.createOrgGroupRequest("Midtown Branch");

        performPost("/api/organizations/org-123/groups", request, workerJwt())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/organizations/{orgId}/groups returns 400 when branch name is blank")
    void testCreateBranchGroup_BlankName_ReturnsBadRequest() throws Exception {
        CreateOrganizationGroupRequest request = CreateOrganizationGroupRequest.builder().name("").build();

        performPost("/api/organizations/org-123/groups", request, ownerJwt())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value("Branch name is required"));
    }

    // ──────────────────────────────────────────────────────────────────
    //  List Branch Groups (ADMIN, OWNER, SUPER_MANAGER, BRANCH_MANAGER)
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/organizations/{orgId}/groups returns 200 when authenticated as SUPER_MANAGER")
    void testListBranchGroups_SuperManagerAuth_ReturnsOk() throws Exception {
        mockListBranchGroups("org-123", "grp-1", "Central Branch");

        performGet("/api/organizations/org-123/groups", superManagerJwt())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("grp-1"))
                .andExpect(jsonPath("$[0].name").value("Central Branch"));
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/groups returns 200 when authenticated as BRANCH_MANAGER")
    void testListBranchGroups_BranchManagerAuth_ReturnsOk() throws Exception {
        mockListBranchGroups("org-123", "grp-2", "West Branch");

        performGet("/api/organizations/org-123/groups", branchManagerJwt())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("grp-2"))
                .andExpect(jsonPath("$[0].name").value("West Branch"));
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/groups returns 200 when authenticated with Keycloak alias branch-manger")
    void testListBranchGroups_BranchMangerAlias_ReturnsOk() throws Exception {
        mockListBranchGroups("org-123", "grp-3", "East Branch");

        performGet("/api/organizations/org-123/groups", branchMangerTypoJwt())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("grp-3"))
                .andExpect(jsonPath("$[0].name").value("East Branch"));
    }

    @Test
    @DisplayName("GET /api/organizations/{orgId}/groups returns 403 when authenticated as WORKER")
    void testListBranchGroups_WorkerAuth_ReturnsForbidden() throws Exception {
        performGet("/api/organizations/org-123/groups", workerJwt())
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────────────────────────────────────────────
    //  Mock Helpers
    // ──────────────────────────────────────────────────────────────────

    private void mockKeycloakListAndCount() {
        RealmResource realmResource = mock(RealmResource.class);
        OrganizationsResource organizationsResource = mock(OrganizationsResource.class);

        OrganizationRepresentation rep = new OrganizationRepresentation();
        rep.setId("org-999");
        rep.setName("Taco House");
        rep.setAlias("taco-house");
        rep.setEnabled(true);

        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.list(0, 20)).thenReturn(List.of(rep));

        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        when(restTemplateBean.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/count"),
                eq(HttpMethod.GET),
                any(),
                eq(Long.class)
        )).thenReturn(ResponseEntity.ok(1L));
    }

    private void mockKeycloakCreateOrg(String orgId) {
        RealmResource realmResource = mock(RealmResource.class);
        OrganizationsResource organizationsResource = mock(OrganizationsResource.class);
        Response response = mock(Response.class);

        when(keycloakAdmin.realm("coresto")).thenReturn(realmResource);
        when(realmResource.organizations()).thenReturn(organizationsResource);
        when(organizationsResource.create(any(OrganizationRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("http://localhost:8081/admin/realms/coresto/organizations/" + orgId);
    }

    private void mockListBranchGroups(String orgId, String groupId, String groupName) {
        var tokenManager = mock(org.keycloak.admin.client.token.TokenManager.class);
        when(keycloakAdmin.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn("test-token");

        Map<String, Object> group = Map.of("id", groupId, "name", groupName);
        when(restTemplateBean.exchange(
                eq("http://localhost:8081/admin/realms/coresto/organizations/" + orgId + "/groups"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(group)));
    }
}
