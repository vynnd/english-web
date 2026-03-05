package com.englishweb.backend.controller;

import com.englishweb.backend.service.WeeklyReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/weekly-review")
public class WeeklyReviewController {

    private final WeeklyReviewService weeklyReviewService;

    @Autowired
    public WeeklyReviewController(WeeklyReviewService weeklyReviewService) {
        this.weeklyReviewService = weeklyReviewService;
    }

    record CompleteTaskRequest(UUID applicationTaskId) {}

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCurrentReview(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(weeklyReviewService.getCurrentWeekReview(userId));
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(weeklyReviewService.getTasks(userId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(@AuthenticationPrincipal UserDetails ud,
                                                             @PathVariable UUID taskId,
                                                             @RequestBody(required = false) CompleteTaskRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        UUID appTaskId = req != null ? req.applicationTaskId() : null;
        return ok(weeklyReviewService.completeTask(userId, taskId, appTaskId));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
