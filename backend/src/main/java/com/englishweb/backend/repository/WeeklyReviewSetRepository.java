package com.englishweb.backend.repository;

import com.englishweb.backend.entity.WeeklyReviewSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyReviewSetRepository extends JpaRepository<WeeklyReviewSet, UUID> {
    Optional<WeeklyReviewSet> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
