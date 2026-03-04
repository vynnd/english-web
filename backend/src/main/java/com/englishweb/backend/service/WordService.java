package com.englishweb.backend.service;

import com.englishweb.backend.entity.UserVocabulary;
import com.englishweb.backend.entity.Word;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.UserVocabularyRepository;
import com.englishweb.backend.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    public Word getWordByText(String wordText) {
        return wordRepository.findByWordIgnoreCase(wordText)
                .orElseThrow(() -> new ResourceNotFoundException("Word not found: " + wordText));
    }

    public boolean isSavedByUser(UUID userId, UUID wordId) {
        return userVocabularyRepository.existsByUserIdAndWordId(userId, wordId);
    }

    public UUID getUserVocabularyId(UUID userId, UUID wordId) {
        return userVocabularyRepository.findByUserIdAndWordId(userId, wordId)
                .map(UserVocabulary::getId)
                .orElse(null);
    }
}
