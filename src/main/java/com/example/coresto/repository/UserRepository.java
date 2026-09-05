package com.example.coresto.repository;

import com.example.coresto.domain.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailNormalized(String emailNormalized);
}
