package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leaderboard_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private LocalDate periodStart;

    private LocalDate periodEnd;

    @Column(nullable = false)
    private Boolean promoted = false;

    @CreationTimestamp
    private Instant snapshotAt;
}
