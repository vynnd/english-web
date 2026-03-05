package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "weekly_missions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private Integer easyWordsTarget = 20;

    @Column(nullable = false)
    private Integer easyWordsAchieved = 0;

    @Column(nullable = false)
    private Integer appReviewTarget = 6;

    @Column(nullable = false)
    private Integer appReviewAchieved = 0;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    private Integer diceCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dice_reward_id")
    private Reward diceReward;

    private Instant completedAt;
}
