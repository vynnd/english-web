package com.englishweb.backend.repository;

import com.englishweb.backend.entity.WeeklyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyMissionRepository extends JpaRepository<WeeklyMission, UUID> {
    Optional<WeeklyMission> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
