package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_vocabulary",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    // SRS fields
    @Column(nullable = false, columnDefinition = "memory_state")
    private String memoryState = "NEW"; // NEW|LEARNING|REVIEW|MASTERED|RELEARNING

    @Column(nullable = false)
    private Integer srsIntervalMinutes = 0;

    private Instant srsDueAt;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    private Instant lastReviewedAt;

    // Application assessment
    @Column(nullable = false)
    private Integer applicationLevel = 0; // 0=locked, 1-6

    private Instant applicationUnlockedAt;

    @CreationTimestamp
    private Instant savedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_article_id")
    private Article sourceArticle;
}
