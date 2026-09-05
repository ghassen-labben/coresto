package com.example.coresto.repository;

import com.example.coresto.domain.common.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}
