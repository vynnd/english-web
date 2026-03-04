package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final WordClickEventRepository wordClickEventRepository;
    private final ArticleRepository articleRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final ProgressService progressService;

    @Transactional
    public ReadingSession startSession(UUID userId, UUID articleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Article article = articleRepository.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        ReadingSession session = ReadingSession.builder()
                .user(user).article(article).build();
        return readingSessionRepository.save(session);
    }

    @Transactional
    public ReadingSession endSession(UUID userId, UUID sessionId, int durationSeconds) {
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Session not found");

        session.setEndedAt(Instant.now());
        session.setDurationSeconds(durationSeconds);

        // Update daily progress reading leg
        progressService.addReadingSeconds(userId, durationSeconds);

        return readingSessionRepository.save(session);
    }

    @Transactional
    public void trackWordClick(UUID userId, UUID sessionId, UUID wordId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        Word word = wordRepository.findById(wordId).orElseThrow(() -> new ResourceNotFoundException("Word not found"));

        WordClickEvent event = WordClickEvent.builder()
                .user(user).article(session.getArticle()).word(word).readingSession(session).build();
        wordClickEventRepository.save(event);
    }

    public List<Map<String, Object>> getPendingWords(UUID userId, UUID sessionId) {
        List<WordClickEvent> clicks = wordClickEventRepository.findBySessionAndUser(sessionId, userId);
        Set<UUID> seen = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();

        for (WordClickEvent click : clicks) {
            Word word = click.getWord();
            if (seen.add(word.getId())) {
                boolean isSaved = userVocabularyRepository.existsByUserIdAndWordId(userId, word.getId());
                result.add(Map.of(
                        "wordId", word.getId(),
                        "word", word.getWord(),
                        "phonetic", word.getPhonetic() != null ? word.getPhonetic() : "",
                        "definitions", word.getDefinitions(),
                        "isAlreadySaved", isSaved
                ));
            }
        }
        return result;
    }
}
