package com.englishweb.backend.repository;

import com.englishweb.backend.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    @Query("SELECT a FROM Article a WHERE a.isActive = true " +
           "AND (:topicId IS NULL OR a.topic.id = :topicId) " +
           "AND (:level IS NULL OR a.languageLevel = :level) " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> findAllActive(UUID topicId, String level, Pageable pageable);
}
