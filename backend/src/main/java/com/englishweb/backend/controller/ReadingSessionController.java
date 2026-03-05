package com.englishweb.backend.controller;

import com.englishweb.backend.service.ReadingSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reading-sessions")
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;

    @Autowired
    public ReadingSessionController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    record StartRequest(@NotNull UUID articleId) {}
    record EndRequest(int durationSeconds) {}
    record WordClickRequest(@NotNull UUID wordId) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> startSession(@AuthenticationPrincipal UserDetails ud,
                                                             @Valid @RequestBody StartRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        var session = readingSessionService.startSession(userId, req.articleId());
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "sessionId", session.getId(), "startedAt", session.getStartedAt())));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<Map<String, Object>> endSession(@AuthenticationPrincipal UserDetails ud,
                                                           @PathVariable UUID id,
                                                           @RequestBody EndRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        var session = readingSessionService.endSession(userId, id, req.durationSeconds());
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "sessionId", session.getId(),
                "durationSeconds", session.getDurationSeconds(),
                "goalAchieved", session.getGoalAchieved()
        )));
    }

    @PostMapping("/{id}/word-clicks")
    public ResponseEntity<Map<String, Object>> trackWordClick(@AuthenticationPrincipal UserDetails ud,
                                                               @PathVariable UUID id,
                                                               @Valid @RequestBody WordClickRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        readingSessionService.trackWordClick(userId, id, req.wordId());
        return ResponseEntity.ok(Map.of("success", true, "data", "Tracked"));
    }

    @GetMapping("/{id}/pending-words")
    public ResponseEntity<Map<String, Object>> getPendingWords(@AuthenticationPrincipal UserDetails ud,
                                                                @PathVariable UUID id) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "data",
                readingSessionService.getPendingWords(userId, id)));
    }
}
