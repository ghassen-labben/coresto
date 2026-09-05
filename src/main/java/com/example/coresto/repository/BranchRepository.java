package com.example.coresto.repository;

import com.example.coresto.domain.tenancy.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByOrganisationId(UUID organisationId);
}
