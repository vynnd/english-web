package com.englishweb.backend.repository;

import com.englishweb.backend.entity.SrsReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface SrsReviewRepository extends JpaRepository<SrsReview, UUID> {
    @Query("SELECT COUNT(r) FROM SrsReview r WHERE r.userId = :userId AND r.reviewedAt >= :from AND r.reviewedAt < :to")
    int countReviewsToday(UUID userId, Instant from, Instant to);
}
