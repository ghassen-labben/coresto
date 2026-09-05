package com.example.coresto.repository;

import com.example.coresto.domain.identity.OrganisationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationMemberRepository extends JpaRepository<OrganisationMember, UUID> {
    Optional<OrganisationMember> findByOrganisationIdAndUserId(UUID organisationId, UUID userId);
    List<OrganisationMember> findByUserId(UUID userId);
}
