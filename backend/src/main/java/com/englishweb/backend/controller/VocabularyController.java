package com.englishweb.backend.controller;

import com.englishweb.backend.service.VocabularyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @Autowired
    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    record SaveRequest(@NotEmpty List<UUID> wordIds, UUID sourceArticleId) {}

    @GetMapping("/daily-limit")
    public ResponseEntity<Map<String, Object>> getDailyLimit(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(vocabularyService.getDailyLimit(userId));
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveWords(@AuthenticationPrincipal UserDetails ud,
                                                          @Valid @RequestBody SaveRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(vocabularyService.saveWords(userId, req.wordIds(), req.sourceArticleId()));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getVocabulary(@AuthenticationPrincipal UserDetails ud,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(ud.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());
        return ok(vocabularyService.getVocabulary(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEntry(@AuthenticationPrincipal UserDetails ud,
                                                         @PathVariable UUID id) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(vocabularyService.getVocabularyEntry(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEntry(@AuthenticationPrincipal UserDetails ud,
                                                            @PathVariable UUID id) {
        UUID userId = UUID.fromString(ud.getUsername());
        vocabularyService.deleteVocabularyEntry(userId, id);
        return ok("Deleted");
    }

    @GetMapping("/saved-in-article/{articleId}")
    public ResponseEntity<Map<String, Object>> getSavedInArticle(@AuthenticationPrincipal UserDetails ud,
                                                                   @PathVariable UUID articleId) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(vocabularyService.getSavedWordsInArticle(userId, articleId));
    }

    @GetMapping("/due")
    public ResponseEntity<Map<String, Object>> getDueForReview(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(vocabularyService.getDueForReview(userId));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
