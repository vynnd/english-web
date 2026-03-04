package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reading_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @CreationTimestamp
    private Instant startedAt;

    private Instant endedAt;
    private Integer durationSeconds;

    @Column(nullable = false)
    private Boolean goalAchieved = false;
}
