package com.example.coresto.repository;

import com.example.coresto.domain.tenancy.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DiningTableRepository extends JpaRepository<DiningTable, UUID> {
    List<DiningTable> findByBranchId(UUID branchId);
}
