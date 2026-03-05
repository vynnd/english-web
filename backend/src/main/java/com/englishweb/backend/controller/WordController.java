package com.englishweb.backend.controller;

import com.englishweb.backend.entity.Word;
import com.englishweb.backend.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/words")
public class WordController {

    private final WordService wordService;

    @Autowired
    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    // Single-click: quick tooltip (id + pronunciation + short meaning)
    @GetMapping("/{word}/tooltip")
    public ResponseEntity<Map<String, Object>> getTooltip(@PathVariable String word) {
        Word w = wordService.getWordByText(word);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", w.getId());
        data.put("word", w.getWord());
        data.put("phonetic", w.getPhonetic() != null ? w.getPhonetic() : "");
        data.put("audioUrl", w.getAudioUrl() != null ? w.getAudioUrl() : "");
        data.put("partOfSpeech", w.getPartOfSpeech() != null ? w.getPartOfSpeech() : "");
        data.put("vnMeaning", w.getVnMeaning() != null ? w.getVnMeaning() : "");
        data.put("definitions", wordService.parseJson(w.getDefinitions()));
        return ok(data);
    }

    // Hold 1.5s: full card with examples, collocations, saved status
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

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", w.getId());
        data.put("word", w.getWord());
        data.put("phonetic", w.getPhonetic() != null ? w.getPhonetic() : "");
        data.put("audioUrl", w.getAudioUrl() != null ? w.getAudioUrl() : "");
        data.put("partOfSpeech", w.getPartOfSpeech() != null ? w.getPartOfSpeech() : "");
        data.put("definitions", wordService.parseJson(w.getDefinitions()));
        data.put("examples", wordService.parseJson(w.getExamples()));
        data.put("collocations", wordService.parseJson(w.getCollocations()));
        data.put("vnMeaning", w.getVnMeaning() != null ? w.getVnMeaning() : "");
        data.put("isSavedByUser", isSaved);
        data.put("userVocabularyId", uvId != null ? uvId : "");
        return ok(data);
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
