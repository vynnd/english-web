package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "srs_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SrsReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id", nullable = false)
    private UserVocabulary userVocabulary;

    @Column(nullable = false)
    private UUID userId; // denormalized

    @Column(nullable = false)
    private Integer responseTimeMs;

    @Column(nullable = false, length = 10)
    private String grade; // EASY|GOOD|HARD|AGAIN

    @Column(nullable = false, length = 10)
    private String phase; // LEARNING|REVIEW

    private Integer previousIntervalMinutes;
    private Integer newIntervalMinutes;

    @Column(insertable = false, updatable = false)
    private Instant reviewedAt;
}
