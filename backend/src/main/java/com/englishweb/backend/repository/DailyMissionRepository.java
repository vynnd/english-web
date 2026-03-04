package com.englishweb.backend.repository;

import com.englishweb.backend.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyMissionRepository extends JpaRepository<DailyMission, UUID> {
    Optional<DailyMission> findByUserIdAndDate(UUID userId, LocalDate date);
}
