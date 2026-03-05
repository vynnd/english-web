package com.englishweb.backend.repository;

import com.englishweb.backend.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, UUID> {
    @Query("SELECT COALESCE(SUM(r.durationSeconds), 0) FROM ReadingSession r WHERE r.user.id = :userId AND r.startedAt >= :from AND r.startedAt < :to")
    Integer sumDurationByUserAndDateRange(UUID userId, Instant from, Instant to);
}
