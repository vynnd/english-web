package com.englishweb.backend.controller;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDaily(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        DailyMission m = missionService.getTodayMission(userId);
        return ok(Map.of(
                "date", m.getDate(),
                "easyWordsTarget", m.getEasyWordsTarget(),
                "easyWordsAchieved", m.getEasyWordsAchieved(),
                "appReviewTarget", m.getAppReviewTarget(),
                "appReviewAchieved", m.getAppReviewAchieved(),
                "isCompleted", m.getIsCompleted(),
                "spinClaimed", m.getSpinReward() != null
        ));
    }

    @PostMapping("/daily/claim")
    public ResponseEntity<Map<String, Object>> claimDailySpin(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        Reward reward = missionService.claimDailySpin(userId);
        return ok(Map.of(
                "rewardId", reward.getId(),
                "source", reward.getRewardSource(),
                "catalog", reward.getCatalog() != null ? reward.getCatalog().getName() : "Surprise!"
        ));
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeekly(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        WeeklyMission m = missionService.getThisWeekMission(userId);
        return ok(Map.of(
                "weekStart", m.getWeekStart(),
                "easyWordsTarget", m.getEasyWordsTarget(),
                "easyWordsAchieved", m.getEasyWordsAchieved(),
                "appReviewTarget", m.getAppReviewTarget(),
                "appReviewAchieved", m.getAppReviewAchieved(),
                "isCompleted", m.getIsCompleted(),
                "diceClaimed", m.getDiceReward() != null
        ));
    }

    @PostMapping("/weekly/claim")
    public ResponseEntity<Map<String, Object>> claimWeeklyDice(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        Reward reward = missionService.claimWeeklyDice(userId);
        return ok(Map.of(
                "rewardId", reward.getId(),
                "diceCount", reward.getDiceCount(),
                "source", reward.getRewardSource()
        ));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
