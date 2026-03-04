package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_missions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer easyWordsTarget = 5;

    @Column(nullable = false)
    private Integer easyWordsAchieved = 0;

    @Column(nullable = false)
    private Integer appReviewTarget = 1;

    @Column(nullable = false)
    private Integer appReviewAchieved = 0;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spin_reward_id")
    private Reward spinReward;

    private Instant completedAt;
}
