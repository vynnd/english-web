package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rewards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id")
    private RewardCatalog catalog;

    @Column(nullable = false, length = 20)
    private String rewardSource; // DAILY_SPIN|WEEKLY_DICE

    private Integer diceCount;

    @Column(insertable = false, updatable = false)
    private Instant earnedAt;

    private Instant redeemedAt;
}
