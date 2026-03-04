package com.englishweb.backend.controller;

import com.englishweb.backend.service.SrsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/srs")
@RequiredArgsConstructor
public class SrsReviewController {

    private final SrsService srsService;

    record ReviewRequest(@NotNull UUID userVocabularyId, @Min(0) int responseTimeMs) {}

    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(@AuthenticationPrincipal UserDetails ud,
                                                             @Valid @RequestBody ReviewRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        var result = srsService.submitReview(userId, req.userVocabularyId(), req.responseTimeMs());
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    }
}
