package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "application_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id", nullable = false)
    private UserVocabulary userVocabulary;

    @Column(nullable = false)
    private UUID userId; // denormalized

    @Column(nullable = false)
    private Integer level; // 1-6

    @Column(nullable = false, length = 50)
    private String taskType; // FLASHCARD|WRITE_SENTENCE|CLOZE_TEST|MULTIPLE_CHOICE|COLLOCATION|WRITE_PARAGRAPH|REACTIVATION

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String userResponse;

    private Boolean isCorrect;
    private Double aiNaturalnessScore;
    private Boolean isForcedUsage;
    private Integer responseTimeMs;
    private LocalDate weekReference;
    private Instant completedAt;

    @CreationTimestamp
    private Instant createdAt;
}
