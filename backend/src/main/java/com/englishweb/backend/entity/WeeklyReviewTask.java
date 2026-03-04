package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "weekly_review_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyReviewTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_review_set_id", nullable = false)
    private WeeklyReviewSet weeklyReviewSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id", nullable = false)
    private UserVocabulary userVocabulary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_task_id")
    private ApplicationTask applicationTask;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    private Instant completedAt;
}
