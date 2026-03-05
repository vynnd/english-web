package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "weekly_review_sets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyReviewSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate weekStart;

    // [{user_vocabulary_id, word_id, selection_reason}]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String selectedWords = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String fixedReward;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    private Instant completedAt;

    @Column(insertable = false, updatable = false)
    private Instant createdAt;
}
