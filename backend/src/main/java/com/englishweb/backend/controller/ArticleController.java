package com.englishweb.backend.controller;

import com.englishweb.backend.entity.Article;
import com.englishweb.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getArticles(
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> result = articleService.getArticles(topicId, level, pageable);

        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "content", result.getContent().stream().map(this::toListDto).toList(),
                "page", result.getNumber(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getArticle(@PathVariable UUID id) {
        Article a = articleService.getArticle(id);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "id", a.getId(), "title", a.getTitle(), "content", a.getContent(),
                "topicId", a.getTopic() != null ? a.getTopic().getId() : null,
                "languageLevel", a.getLanguageLevel() != null ? a.getLanguageLevel() : "",
                "wordCount", a.getWordCount() != null ? a.getWordCount() : 0,
                "estimatedReadSeconds", a.getEstimatedReadSeconds() != null ? a.getEstimatedReadSeconds() : 0,
                "publishedAt", a.getPublishedAt()
        )));
    }

    private Map<String, Object> toListDto(Article a) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", a.getId());
        dto.put("title", a.getTitle());
        dto.put("topicId", a.getTopic() != null ? a.getTopic().getId() : null);
        dto.put("languageLevel", a.getLanguageLevel());
        dto.put("wordCount", a.getWordCount());
        dto.put("estimatedReadSeconds", a.getEstimatedReadSeconds());
        dto.put("publishedAt", a.getPublishedAt());
        return dto;
    }
}
