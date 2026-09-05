package com.example.coresto.repository;

import com.example.coresto.domain.tenancy.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
    Optional<Organisation> findByAlias(String alias);
}
