package com.englishweb.backend.controller;

import com.englishweb.backend.entity.User;
import com.englishweb.backend.entity.UserGoal;
import com.englishweb.backend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    record UpdateUsernameRequest(@NotBlank String username) {}
    record UpdateGoalRequest(@Min(1) @Max(480) int readingMinutesGoal, @Min(1) @Max(50) int vocabCountGoal) {}

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.getUser(userId);
        return ok(Map.of(
                "id", user.getId(), "email", user.getEmail(), "username", user.getUsername(),
                "isPremium", user.getIsPremium(), "totalPoints", user.getTotalPoints(),
                "tier", user.getTier(), "currentStreak", user.getCurrentStreak(),
                "longestStreak", user.getLongestStreak(), "dailyWordLimit", user.getDailyWordLimit()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(@AuthenticationPrincipal UserDetails userDetails,
                                                         @Valid @RequestBody UpdateUsernameRequest req) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.updateUsername(userId, req.username());
        return ok(Map.of("username", user.getUsername()));
    }

    @GetMapping("/me/goals")
    public ResponseEntity<Map<String, Object>> getGoals(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        UserGoal goal = userService.getCurrentGoal(userId);
        return ok(Map.of(
                "readingMinutesGoal", goal.getReadingMinutesGoal(),
                "vocabCountGoal", goal.getVocabCountGoal(),
                "effectiveFrom", goal.getEffectiveFrom()
        ));
    }

    @PutMapping("/me/goals")
    public ResponseEntity<Map<String, Object>> updateGoals(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody UpdateGoalRequest req) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        UserGoal goal = userService.updateGoal(userId, req.readingMinutesGoal(), req.vocabCountGoal());
        return ok(Map.of(
                "readingMinutesGoal", goal.getReadingMinutesGoal(),
                "vocabCountGoal", goal.getVocabCountGoal(),
                "effectiveFrom", goal.getEffectiveFrom()
        ));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
