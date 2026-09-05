package com.example.coresto.base;

import com.example.coresto.dto.organization.CreateOrganizationGroupRequest;
import com.example.coresto.dto.organization.CreateOrganizationRequest;
import com.example.coresto.dto.organization.OrganizationGroupResponse;
import com.example.coresto.dto.organization.OrganizationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Factory for creating reusable sample test entities and DTOs across unit and integration tests.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // utility class
    }

    public static CreateOrganizationRequest createOrgRequest() {
        return createOrgRequest("Test Restaurant");
    }

    public static CreateOrganizationRequest createOrgRequest(String name) {
        return CreateOrganizationRequest.builder()
                .name(name)
                .alias(name.toLowerCase().replace(" ", "-"))
                .domains(List.of("testrestaurant.com"))
                .build();
    }

    public static CreateOrganizationRequest createOrgRequest(String name, String alias, List<String> domains) {
        return CreateOrganizationRequest.builder()
                .name(name)
                .alias(alias)
                .domains(domains)
                .build();
    }

    public static OrganizationResponse createOrgResponse(String id, String name, String alias) {
        return OrganizationResponse.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .name(name)
                .alias(alias != null ? alias : name.toLowerCase().replace(" ", "-"))
                .enabled(true)
                .domains(List.of(name.toLowerCase().replace(" ", "") + ".com"))
                .build();
    }

    public static CreateOrganizationGroupRequest createOrgGroupRequest() {
        return createOrgGroupRequest("Downtown Branch");
    }

    public static CreateOrganizationGroupRequest createOrgGroupRequest(String branchName) {
        return CreateOrganizationGroupRequest.builder()
                .name(branchName)
                .build();
    }

    public static OrganizationGroupResponse createOrgGroupResponse(String id, String name, String orgId) {
        return OrganizationGroupResponse.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .name(name)
                .organizationId(orgId != null ? orgId : UUID.randomUUID().toString())
                .build();
    }
}
