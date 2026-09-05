package com.example.coresto.repository;

import com.example.coresto.domain.catalog.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    @Query("SELECT m FROM Menu m JOIN m.menuBranches mb WHERE mb.branch.id = :branchId AND m.status = 'PUBLISHED' AND mb.active = true")
    List<Menu> findPublishedMenusByBranchId(@Param("branchId") UUID branchId);
}
