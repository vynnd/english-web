package com.englishweb.backend.controller;

import com.englishweb.backend.service.LeaderboardService;
import com.englishweb.backend.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final PointsService pointsService;

    @Autowired
    public LeaderboardController(LeaderboardService leaderboardService, PointsService pointsService) {
        this.leaderboardService = leaderboardService;
        this.pointsService = pointsService;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "BEGINNER") String tier) {
        return ok(leaderboardService.getLeaderboard(tier));
    }

    @GetMapping("/leaderboard/me")
    public ResponseEntity<Map<String, Object>> getMyRank(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(leaderboardService.getMyRank(userId));
    }

    @GetMapping("/points/history")
    public ResponseEntity<Map<String, Object>> getPointsHistory(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(ud.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        var result = pointsService.getHistory(userId, pageable);
        return ok(Map.of("content", result.getContent(), "page", result.getNumber(), "totalPages", result.getTotalPages()));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
