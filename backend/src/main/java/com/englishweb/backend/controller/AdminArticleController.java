package com.englishweb.backend.controller;

import com.englishweb.backend.entity.Article;
import com.englishweb.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    @Autowired
    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createArticle(@RequestBody Map<String, Object> body) {
        UUID topicId = UUID.fromString((String) body.get("topicId"));
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String level = (String) body.get("level");
        String sourceUrl = (String) body.get("sourceUrl");

        Article article = articleService.createArticle(topicId, title, content, level, sourceUrl);
        return ResponseEntity.ok(Map.of("success", true, "data", toDto(article)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateArticle(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        UUID topicId = body.get("topicId") != null ? UUID.fromString((String) body.get("topicId")) : null;
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String level = (String) body.get("level");
        String sourceUrl = (String) body.get("sourceUrl");

        Article article = articleService.updateArticle(id, topicId, title, content, level, sourceUrl);
        return ResponseEntity.ok(Map.of("success", true, "data", toDto(article)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> toDto(Article a) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("id", a.getId());
        dto.put("title", a.getTitle());
        dto.put("content", a.getContent());
        dto.put("topicId", a.getTopic() != null ? a.getTopic().getId() : null);
        dto.put("topicName", a.getTopic() != null ? a.getTopic().getName() : null);
        dto.put("languageLevel", a.getLanguageLevel());
        dto.put("sourceUrl", a.getSourceUrl());
        dto.put("wordCount", a.getWordCount());
        dto.put("isActive", a.getIsActive());
        dto.put("publishedAt", a.getPublishedAt());
        return dto;
    }
}
