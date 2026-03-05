package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "words")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 200)
    private String word;

    @Column(length = 200)
    private String phonetic;

    @Column(length = 500)
    private String audioUrl;

    @Column(length = 50)
    private String partOfSpeech;

    @Column(columnDefinition = "TEXT")
    private String vnMeaning;

    // [{lang:"en", meaning:"..."}]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String definitions = "[]";

    // [{sentence:"...", translation:"..."}]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String examples = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String collocations = "[]";

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
