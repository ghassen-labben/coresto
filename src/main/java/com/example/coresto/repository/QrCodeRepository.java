package com.example.coresto.repository;

import com.example.coresto.domain.tenancy.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    Optional<QrCode> findByPublicTokenAndActiveTrue(String publicToken);
}
