package com.englishweb.backend.service;

import com.englishweb.backend.entity.LeaderboardSnapshot;
import com.englishweb.backend.repository.LeaderboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardSnapshotRepository leaderboardSnapshotRepository;

    public List<LeaderboardSnapshot> getLeaderboard(String tier) {
        LocalDate latestPeriod = LocalDate.now().withDayOfMonth(1);
        return leaderboardSnapshotRepository.findByTierAndPeriodStartOrderByRankAsc(tier, latestPeriod);
    }

    public Map<String, Object> getMyRank(UUID userId) {
        return leaderboardSnapshotRepository.findLatestByUserId(userId)
                .map(s -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("tier", s.getTier());
                    result.put("points", s.getPoints());
                    result.put("rank", s.getRank());
                    result.put("promoted", s.getPromoted());
                    result.put("periodStart", s.getPeriodStart());
                    return result;
                })
                .orElseGet(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("message", "Not yet ranked — keep earning points!");
                    return result;
                });
    }
}
