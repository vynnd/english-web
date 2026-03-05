package com.englishweb.backend.controller;

import com.englishweb.backend.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;

    @Autowired
    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHistory(@AuthenticationPrincipal UserDetails ud,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(ud.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        var result = rewardService.getRewardHistory(userId, pageable);
        return ok(Map.of("content", result.getContent(), "page", result.getNumber(), "totalPages", result.getTotalPages()));
    }

    @GetMapping("/catalog")
    public ResponseEntity<Map<String, Object>> getCatalog() {
        return ok(rewardService.getCatalog());
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
