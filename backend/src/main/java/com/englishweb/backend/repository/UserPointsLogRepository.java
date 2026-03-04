package com.englishweb.backend.repository;

import com.englishweb.backend.entity.UserPointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPointsLogRepository extends JpaRepository<UserPointsLog, UUID> {
    Page<UserPointsLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
