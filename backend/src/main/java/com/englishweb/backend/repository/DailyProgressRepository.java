package com.englishweb.backend.repository;

import com.englishweb.backend.entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, UUID> {
    Optional<DailyProgress> findByUserIdAndDate(UUID userId, LocalDate date);
    List<DailyProgress> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDate from, LocalDate to);
}
