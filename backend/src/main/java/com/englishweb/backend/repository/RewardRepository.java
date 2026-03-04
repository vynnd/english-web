package com.englishweb.backend.repository;

import com.englishweb.backend.entity.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {
    Page<Reward> findByUserIdOrderByEarnedAtDesc(UUID userId, Pageable pageable);
}
