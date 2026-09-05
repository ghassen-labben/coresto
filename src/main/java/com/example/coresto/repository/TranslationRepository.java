package com.example.coresto.repository;

import com.example.coresto.domain.catalog.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TranslationRepository extends JpaRepository<Translation, UUID> {
    List<Translation> findByEntityTypeAndEntityId(String entityType, UUID entityId);
}
