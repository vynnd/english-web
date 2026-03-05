package com.englishweb.backend.repository;

import com.englishweb.backend.entity.RewardCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardCatalogRepository extends JpaRepository<RewardCatalog, UUID> {
    List<RewardCatalog> findByIsActiveTrueOrderBySpinProbabilityDesc();
}
