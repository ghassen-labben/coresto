package com.example.coresto.repository;

import com.example.coresto.domain.ordering.DiningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DiningSessionRepository extends JpaRepository<DiningSession, UUID> {
}
