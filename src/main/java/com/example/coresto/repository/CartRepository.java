package com.example.coresto.repository;

import com.example.coresto.domain.ordering.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findBySessionId(UUID sessionId);
}
