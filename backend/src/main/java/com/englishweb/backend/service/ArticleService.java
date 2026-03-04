package com.englishweb.backend.service;

import com.englishweb.backend.entity.Article;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Page<Article> getArticles(UUID topicId, String level, Pageable pageable) {
        return articleRepository.findAllActive(topicId, level, pageable);
    }

    public Article getArticle(UUID id) {
        return articleRepository.findById(id)
                .filter(Article::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }
}
