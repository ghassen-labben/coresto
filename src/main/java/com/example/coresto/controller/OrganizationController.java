package com.example.coresto.controller;

import com.example.coresto.dto.organization.CreateOrganizationGroupRequest;
import com.example.coresto.dto.organization.CreateOrganizationRequest;
import com.example.coresto.dto.organization.OrganizationGroupResponse;
import com.example.coresto.dto.organization.OrganizationResponse;
import com.example.coresto.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Manage organizations (restaurants/coffee shops) and their groups (branches)")
public class OrganizationController {

    private final OrganizationService organizationService;

    // ──────────────────────────────────────────────────────────────────
    //  Organizations
    // ──────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new organization", description = "Creates a restaurant, coffee shop, or other establishment as a Keycloak organization")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<List<OrganizationResponse>> listOrganizations() {
        return ResponseEntity.ok(organizationService.listOrganizations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable String id) {
        return ResponseEntity.ok(organizationService.getOrganization(id));
    }

    // ──────────────────────────────────────────────────────────────────
    //  Organization Groups (Branches)
    // ──────────────────────────────────────────────────────────────────

    @PostMapping("/{orgId}/groups")
    @Operation(summary = "Create a group (branch) within an organization")
    public ResponseEntity<OrganizationGroupResponse> createOrganizationGroup(
            @PathVariable String orgId,
            @RequestBody CreateOrganizationGroupRequest request
    ) {
        OrganizationGroupResponse response = organizationService.createOrganizationGroup(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orgId}/groups")
    @Operation(summary = "List all groups (branches) for an organization")
    public ResponseEntity<List<OrganizationGroupResponse>> listOrganizationGroups(
            @PathVariable String orgId
    ) {
        return ResponseEntity.ok(organizationService.listOrganizationGroups(orgId));
    }
}
