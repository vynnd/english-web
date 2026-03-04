package com.englishweb.backend.controller;

import com.englishweb.backend.entity.Word;
import com.englishweb.backend.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    // Single-click: quick tooltip (pronunciation + short meaning)
    @GetMapping("/{word}/tooltip")
    public ResponseEntity<Map<String, Object>> getTooltip(@PathVariable String word) {
        Word w = wordService.getWordByText(word);
        return ok(Map.of(
                "word", w.getWord(),
                "phonetic", w.getPhonetic() != null ? w.getPhonetic() : "",
                "partOfSpeech", w.getPartOfSpeech() != null ? w.getPartOfSpeech() : "",
                "definitions", w.getDefinitions()
        ));
    }

    // Hold 1-1.5s: full card with examples, collocations, saved status
    @GetMapping("/{word}/detail")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable String word,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        Word w = wordService.getWordByText(word);
        boolean isSaved = false;
        UUID uvId = null;
        if (userDetails != null) {
            UUID userId = UUID.fromString(userDetails.getUsername());
            isSaved = wordService.isSavedByUser(userId, w.getId());
            uvId = wordService.getUserVocabularyId(userId, w.getId());
        }

        return ok(Map.of(
                "id", w.getId(),
                "word", w.getWord(),
                "phonetic", w.getPhonetic() != null ? w.getPhonetic() : "",
                "partOfSpeech", w.getPartOfSpeech() != null ? w.getPartOfSpeech() : "",
                "definitions", w.getDefinitions(),
                "examples", w.getExamples(),
                "collocations", w.getCollocations(),
                "isSavedByUser", isSaved,
                "userVocabularyId", uvId != null ? uvId : ""
        ));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
