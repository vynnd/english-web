package com.englishweb.backend.repository;

import com.englishweb.backend.entity.LeaderboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, UUID> {
    List<LeaderboardSnapshot> findByTierAndPeriodStartOrderByRankAsc(String tier, LocalDate periodStart);

    @Query("SELECT s FROM LeaderboardSnapshot s WHERE s.user.id = :userId ORDER BY s.periodStart DESC LIMIT 1")
    Optional<LeaderboardSnapshot> findLatestByUserId(UUID userId);
}
