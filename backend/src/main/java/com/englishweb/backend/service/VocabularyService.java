package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.BadRequestException;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class VocabularyService {

    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final ArticleRepository articleRepository;
    private final ProgressService progressService;

    @Autowired
    public VocabularyService(UserVocabularyRepository userVocabularyRepository,
                              UserRepository userRepository,
                              WordRepository wordRepository,
                              ArticleRepository articleRepository,
                              ProgressService progressService) {
        this.userVocabularyRepository = userVocabularyRepository;
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.articleRepository = articleRepository;
        this.progressService = progressService;
    }

    public Map<String, Object> getDailyLimit(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = startOfDay.plusSeconds(86400);
        int used = userVocabularyRepository.countSavedToday(userId, startOfDay, endOfDay);
        int limit = user.getDailyWordLimit();
        return Map.of("used", used, "limit", limit, "remaining", Math.max(0, limit - used), "isPremium", user.getIsPremium());
    }

    @Transactional
    public Map<String, Object> saveWords(UUID userId, List<UUID> wordIds, UUID sourceArticleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = startOfDay.plusSeconds(86400);
        int used = userVocabularyRepository.countSavedToday(userId, startOfDay, endOfDay);
        int limit = user.getDailyWordLimit();
        int remaining = limit - used;

        Article sourceArticle = sourceArticleId != null
                ? articleRepository.findById(sourceArticleId).orElse(null) : null;

        List<UUID> saved = new ArrayList<>();
        List<UUID> skipped = new ArrayList<>();

        for (UUID wordId : wordIds) {
            if (userVocabularyRepository.existsByUserIdAndWordId(userId, wordId)) {
                skipped.add(wordId);
                continue;
            }
            if (saved.size() >= remaining) {
                skipped.add(wordId);
                continue;
            }
            Word word = wordRepository.findById(wordId).orElse(null);
            if (word == null) continue;

            UserVocabulary uv = UserVocabulary.builder()
                    .user(user).word(word).memoryState("NEW")
                    .srsDueAt(Instant.now()).srsIntervalMinutes(0)
                    .sourceArticle(sourceArticle).build();
            userVocabularyRepository.save(uv);
            saved.add(wordId);
        }

        progressService.addWordsSaved(userId, saved.size());

        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);
        result.put("skipped", skipped);
        if (remaining - saved.size() <= 0) result.put("limitReached", true);
        return result;
    }

    public Page<UserVocabulary> getVocabulary(UUID userId, Pageable pageable) {
        return userVocabularyRepository.findByUserId(userId, pageable);
    }

    public UserVocabulary getVocabularyEntry(UUID userId, UUID uvId) {
        UserVocabulary uv = userVocabularyRepository.findById(uvId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary entry not found"));
        if (!uv.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Vocabulary entry not found");
        return uv;
    }

    @Transactional
    public void deleteVocabularyEntry(UUID userId, UUID uvId) {
        UserVocabulary uv = getVocabularyEntry(userId, uvId);
        userVocabularyRepository.delete(uv);
    }

    public List<UserVocabulary> getDueForReview(UUID userId) {
        return userVocabularyRepository.findDueForReview(userId, Instant.now());
    }
}
