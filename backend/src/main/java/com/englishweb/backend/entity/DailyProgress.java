package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    // Reading leg
    @Column(nullable = false)
    private Integer readingSeconds = 0;

    @Column(nullable = false)
    private Boolean readingGoalAchieved = false;

    // Vocab learning leg
    @Column(nullable = false)
    private Integer wordsSaved = 0;

    @Column(nullable = false)
    private Integer wordsReachedEasy = 0;

    @Column(nullable = false)
    private Boolean vocabGoalAchieved = false;

    // Review leg
    @Column(nullable = false)
    private Integer reviewsCompleted = 0;

    @Column(nullable = false)
    private Boolean reviewGoalAchieved = false;

    @Column(nullable = false)
    private Boolean streakMaintained = false;

    @Column(nullable = false)
    private Integer streakCount = 0;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
