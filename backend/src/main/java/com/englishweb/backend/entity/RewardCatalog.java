package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reward_catalog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RewardCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    private String description;

    @Column(nullable = false, length = 50)
    private String rewardType; // POINTS|BADGE|EXTRA_WORD_SLOT|PREMIUM_DAY

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String value; // {amount: 100} or {days: 3}

    private Double spinProbability;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(insertable = false, updatable = false)
    private Instant createdAt;
}
