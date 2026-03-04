package com.englishweb.backend.controller;

import com.englishweb.backend.entity.DailyProgress;
import com.englishweb.backend.service.ProgressService;
import com.englishweb.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserService userService;

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getToday(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        DailyProgress p = progressService.getTodayProgress(userId);
        var goal = userService.getCurrentGoal(userId);

        return ok(Map.of(
                "date", LocalDate.now(),
                "reading", Map.of(
                        "seconds", p.getReadingSeconds(),
                        "goalSeconds", goal.getReadingMinutesGoal() * 60,
                        "achieved", p.getReadingGoalAchieved()),
                "vocab", Map.of(
                        "wordsReachedEasy", p.getWordsReachedEasy(),
                        "goal", goal.getVocabCountGoal(),
                        "achieved", p.getVocabGoalAchieved()),
                "review", Map.of(
                        "completed", p.getReviewsCompleted(),
                        "goal", Math.max(1, goal.getVocabCountGoal() / 2),
                        "achieved", p.getReviewGoalAchieved()),
                "streakMaintained", p.getStreakMaintained(),
                "streakCount", p.getStreakCount()
        ));
    }

    @GetMapping("/streak")
    public ResponseEntity<Map<String, Object>> getStreak(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(progressService.getStreak(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(progressService.getHistory(userId, from, to));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
