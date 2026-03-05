package com.englishweb.backend.service;

import com.englishweb.backend.entity.Article;
import com.englishweb.backend.entity.Topic;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.ArticleRepository;
import com.englishweb.backend.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TopicRepository topicRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository, TopicRepository topicRepository) {
        this.articleRepository = articleRepository;
        this.topicRepository = topicRepository;
    }

    public Page<Article> getArticles(UUID topicId, String level, Pageable pageable) {
        return articleRepository.findAllActive(topicId, level, pageable);
    }

    public Article getArticle(UUID id) {
        return articleRepository.findById(id)
                .filter(Article::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    @Transactional
    public Article createArticle(UUID topicId, String title, String content, String level, String sourceUrl) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        Article article = Article.builder()
                .topic(topic)
                .title(title)
                .content(content)
                .languageLevel(level)
                .sourceUrl(sourceUrl)
                .wordCount(content != null ? content.split("\\s+").length : 0)
                .isActive(true)
                .publishedAt(Instant.now())
                .build();
        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(UUID id, UUID topicId, String title, String content, String level, String sourceUrl) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            article.setTopic(topic);
        }
        if (title != null) article.setTitle(title);
        if (content != null) {
            article.setContent(content);
            article.setWordCount(content.split("\\s+").length);
        }
        if (level != null) article.setLanguageLevel(level);
        if (sourceUrl != null) article.setSourceUrl(sourceUrl);
        return articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setIsActive(false);
        articleRepository.save(article);
    }
}
