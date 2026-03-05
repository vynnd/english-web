package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPremium = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer dailyWordLimit = 5;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalPoints = 0;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String tier = "NONE";

    @Builder.Default
    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer longestStreak = 0;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "USER";  // USER | ADMIN

    @Column(insertable = false, updatable = false)
    private Instant createdAt;

    @Column(insertable = false, updatable = false)
    private Instant updatedAt;
}
