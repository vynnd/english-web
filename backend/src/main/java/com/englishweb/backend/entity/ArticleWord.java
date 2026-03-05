package com.englishweb.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "article_words")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(ArticleWord.ArticleWordId.class)
public class ArticleWord {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleWordId implements Serializable {
        private UUID article;
        private UUID word;
    }
}
