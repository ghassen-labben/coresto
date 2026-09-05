package com.example.coresto.repository;

import com.example.coresto.domain.catalog.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
    List<Ingredient> findByOrganisationId(UUID organisationId);
}
