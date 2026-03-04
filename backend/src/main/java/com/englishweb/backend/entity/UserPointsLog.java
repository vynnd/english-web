package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_points_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPointsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer pointsDelta;

    @Column(nullable = false, length = 50)
    private String reason; // WORD_EASY|APP_TASK_COMPLETE|REWARD_REDEEM

    private UUID referenceId;

    @Column(nullable = false)
    private Integer totalAfter;

    @CreationTimestamp
    private Instant createdAt;
}
