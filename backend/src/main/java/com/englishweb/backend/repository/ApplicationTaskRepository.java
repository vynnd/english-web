package com.englishweb.backend.repository;

import com.englishweb.backend.entity.ApplicationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ApplicationTaskRepository extends JpaRepository<ApplicationTask, UUID> {
    @Query("SELECT COUNT(t) FROM ApplicationTask t WHERE t.userId = :userId AND t.completedAt >= :from AND t.completedAt < :to AND t.isCorrect = true")
    int countCompletedToday(UUID userId, Instant from, Instant to);

    List<ApplicationTask> findByUserVocabularyIdOrderByCreatedAtDesc(UUID userVocabularyId);

    @Query("SELECT COUNT(t) FROM ApplicationTask t WHERE t.userVocabulary.id = :uvId AND t.level = :level AND t.isCorrect = true AND t.completedAt >= :from")
    int countCorrectAtLevel(UUID uvId, int level, Instant from);
}
