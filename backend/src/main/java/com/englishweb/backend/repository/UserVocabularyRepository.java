package com.englishweb.backend.repository;

import com.englishweb.backend.entity.UserVocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, UUID> {
    Optional<UserVocabulary> findByUserIdAndWordId(UUID userId, UUID wordId);
    boolean existsByUserIdAndWordId(UUID userId, UUID wordId);

    @Query("SELECT v FROM UserVocabulary v JOIN FETCH v.word WHERE v.user.id = :userId AND v.srsDueAt <= :now AND v.memoryState <> 'MASTERED' ORDER BY v.srsDueAt ASC")
    List<UserVocabulary> findDueForReview(UUID userId, Instant now);

    @Query("SELECT COUNT(v) FROM UserVocabulary v WHERE v.user.id = :userId AND v.savedAt >= :from AND v.savedAt < :to")
    int countSavedToday(UUID userId, Instant from, Instant to);

    @Query("SELECT COUNT(v) FROM UserVocabulary v WHERE v.user.id = :userId AND v.applicationLevel >= 2")
    int countActiveLv2Plus(UUID userId);

    Page<UserVocabulary> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT v FROM UserVocabulary v JOIN FETCH v.word WHERE v.user.id = :userId AND v.applicationLevel >= 2 ORDER BY v.srsDueAt ASC LIMIT 2")
    List<UserVocabulary> findNearDue(UUID userId);

    @Query("SELECT v FROM UserVocabulary v JOIN FETCH v.word WHERE v.user.id = :userId ORDER BY v.applicationLevel DESC LIMIT 1")
    Optional<UserVocabulary> findHighestLevel(UUID userId);

    @Query("SELECT v FROM UserVocabulary v JOIN FETCH v.word WHERE v.user.id = :userId AND v.lastReviewedAt IS NOT NULL ORDER BY v.lastReviewedAt ASC LIMIT 1")
    Optional<UserVocabulary> findLeastRecentlyUsed(UUID userId);

    @Query("SELECT v FROM UserVocabulary v JOIN FETCH v.word WHERE v.user.id = :userId AND v.applicationLevel >= 1 AND v.applicationLevel < 6 ORDER BY v.applicationLevel DESC LIMIT 2")
    List<UserVocabulary> findNearLevelUp(UUID userId);
}
