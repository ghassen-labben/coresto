package com.example.coresto.repository;

import com.example.coresto.domain.catalog.ProductBranchAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductBranchAvailabilityRepository extends JpaRepository<ProductBranchAvailability, UUID> {
    List<ProductBranchAvailability> findByBranchIdAndProductIdIn(UUID branchId, Collection<UUID> productIds);
}
