package com.example.coresto.service;

import com.example.coresto.dto.PaginatedResponse;
import com.example.coresto.dto.organization.CreateOrganizationGroupRequest;
import com.example.coresto.dto.organization.CreateOrganizationRequest;
import com.example.coresto.dto.organization.OrganizationGroupResponse;
import com.example.coresto.dto.organization.OrganizationResponse;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.OrganizationDomainRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing Keycloak organizations (restaurants/coffee shops)
 * and organization groups (branches).
 *
 * <p>Organizations are managed via the Keycloak Admin Client.
 * Organization groups use direct REST calls since the admin client
 * library (26.0.x) may not expose the groups API introduced in server 26.6.0.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final Keycloak keycloakAdmin;
    private final RestTemplate restTemplate;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.target-realm}")
    private String targetRealm;

    // ──────────────────────────────────────────────────────────────────
    //  Organizations
    // ──────────────────────────────────────────────────────────────────

    /**
     * Create a new organization in Keycloak.
     *
     * @return the created organization with its generated ID
     */
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Organization name is required");
        }

        OrganizationRepresentation rep = new OrganizationRepresentation();
        rep.setName(request.getName());
        rep.setAlias(request.getAlias() != null ? request.getAlias() : toAlias(request.getName()));
        rep.setEnabled(true);

        // Set domains if provided
        if (request.getDomains() != null && !request.getDomains().isEmpty()) {
           request.getDomains()
                    .forEach(domain -> {
                        OrganizationDomainRepresentation d = new OrganizationDomainRepresentation();
                        d.setName(domain);
                        rep.addDomain(d);
                    });
        }

        try (Response response = keycloakAdmin.realm(targetRealm).organizations().create(rep)) {
            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                String orgId = extractIdFromLocation(locationHeader);

                log.info("Organization created: name={}, id={}", request.getName(), orgId);

                return OrganizationResponse.builder()
                        .id(orgId)
                        .name(rep.getName())
                        .alias(rep.getAlias())
                        .enabled(true)
                        .domains(request.getDomains())
                        .build();
            } else {
                String body = response.readEntity(String.class);
                throw new RuntimeException("Failed to create organization: HTTP "
                        + response.getStatus() + " — " + body);
            }
        }
    }

    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * List organizations in the realm with pagination.
     *
     * @param first zero-based offset of the first result (defaults to 0)
     * @param max   maximum number of results per page (defaults to 20)
     * @return a paginated response containing organizations and page metadata
     */
    public PaginatedResponse<OrganizationResponse> listOrganizations(Integer first, Integer max) {
        int offset = first != null ? first : 0;
        int pageSize = max != null ? max : DEFAULT_PAGE_SIZE;

        List<OrganizationRepresentation> orgs = keycloakAdmin.realm(targetRealm)
                .organizations()
                .list(offset, pageSize);

        long totalElements = countOrganizations();
        int pageNumber = pageSize > 0 ? offset / pageSize : 0;

        List<OrganizationResponse> content = orgs.stream()
                .map(this::toOrganizationResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.of(content, pageNumber, pageSize, totalElements);
    }

    /**
     * Get a single organization by ID.
     */
    public OrganizationResponse getOrganization(String orgId) {
        OrganizationRepresentation rep = keycloakAdmin.realm(targetRealm)
                .organizations()
                .get(orgId)
                .toRepresentation();

        return toOrganizationResponse(rep);
    }

    // ──────────────────────────────────────────────────────────────────
    //  Organization Groups (via REST API)
    // ──────────────────────────────────────────────────────────────────

    /**
     * Create a group (branch) within an organization.
     * Uses direct REST calls to the Keycloak Admin API.
     */
    public OrganizationGroupResponse createOrganizationGroup(
            String orgId,
            CreateOrganizationGroupRequest request
    ) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }

        String url = buildGroupsUrl(orgId);
        HttpHeaders headers = buildAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("name", request.getName());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
            String groupId = extractIdFromLocation(location);

            log.info("Organization group created: name={}, orgId={}, groupId={}",
                    request.getName(), orgId, groupId);

            return OrganizationGroupResponse.builder()
                    .id(groupId)
                    .name(request.getName())
                    .organizationId(orgId)
                    .build();
        } else {
            throw new RuntimeException("Failed to create organization group: HTTP "
                    + response.getStatusCode());
        }
    }

    /**
     * List all groups (branches) within an organization.
     */
    @SuppressWarnings("unchecked")
    public List<OrganizationGroupResponse> listOrganizationGroups(String orgId) {
        String url = buildGroupsUrl(orgId);
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getBody() == null) {
            return Collections.emptyList();
        }

        return response.getBody().stream()
                .map(group -> OrganizationGroupResponse.builder()
                        .id((String) group.get("id"))
                        .name((String) group.get("name"))
                        .organizationId(orgId)
                        .build())
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────

    private OrganizationResponse toOrganizationResponse(OrganizationRepresentation rep) {
        List<String> domainNames = rep.getDomains() != null
                ? rep.getDomains().stream()
                    .map(OrganizationDomainRepresentation::getName)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return OrganizationResponse.builder()
                .id(rep.getId())
                .name(rep.getName())
                .alias(rep.getAlias())
                .enabled(rep.isEnabled())
                .domains(domainNames)
                .build();
    }

    /**
     * Get the total count of organizations in the realm via the Keycloak REST API.
     */
    private long countOrganizations() {
        String url = buildOrganizationsCountUrl();
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Long.class);

        return response.getBody() != null ? response.getBody() : 0L;
    }

    private String buildOrganizationsCountUrl() {
        return keycloakServerUrl + "/admin/realms/" + targetRealm + "/organizations/count";
    }

    private String buildGroupsUrl(String orgId) {
        return keycloakServerUrl + "/admin/realms/" + targetRealm
                + "/organizations/" + orgId + "/groups";
    }

    private HttpHeaders buildAuthHeaders() {
        String token = keycloakAdmin.tokenManager().getAccessTokenString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String extractIdFromLocation(String locationHeader) {
        if (locationHeader == null || locationHeader.isBlank()) {
            return null;
        }
        // Location header format: .../organizations/{id} or .../groups/{id}
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
    }

    private String toAlias(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
