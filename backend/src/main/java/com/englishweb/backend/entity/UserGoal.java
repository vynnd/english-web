package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer readingMinutesGoal = 10;

    @Column(nullable = false)
    private Integer vocabCountGoal = 5;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column(insertable = false, updatable = false)
    private Instant createdAt;
}
