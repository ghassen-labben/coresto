package com.example.coresto.repository;

import com.example.coresto.domain.platform.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByOrganisationId(UUID organisationId);
}
