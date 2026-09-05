package com.example.coresto.repository;

import com.example.coresto.domain.ordering.GuestDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GuestDeviceRepository extends JpaRepository<GuestDevice, UUID> {
    Optional<GuestDevice> findByPublicToken(String publicToken);
    Optional<GuestDevice> findByPhoneE164(String phoneE164);
    Optional<GuestDevice> findByEmailNormalized(String emailNormalized);
}
