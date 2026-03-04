package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final RewardCatalogRepository rewardCatalogRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reward grantSpin(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        RewardCatalog catalog = spinWheel();
        Reward reward = Reward.builder()
                .user(user).catalog(catalog).rewardSource("DAILY_SPIN").build();
        return rewardRepository.save(reward);
    }

    @Transactional
    public Reward grantDice(UUID userId, int diceCount) {
        User user = userRepository.findById(userId).orElseThrow();
        Reward reward = Reward.builder()
                .user(user).rewardSource("WEEKLY_DICE").diceCount(diceCount).build();
        return rewardRepository.save(reward);
    }

    private RewardCatalog spinWheel() {
        List<RewardCatalog> catalog = rewardCatalogRepository.findByIsActiveTrueOrderBySpinProbabilityDesc();
        if (catalog.isEmpty()) return null;
        double rand = Math.random();
        double cumulative = 0;
        for (RewardCatalog item : catalog) {
            if (item.getSpinProbability() != null) {
                cumulative += item.getSpinProbability();
                if (rand <= cumulative) return item;
            }
        }
        return catalog.get(0);
    }

    public Page<Reward> getRewardHistory(UUID userId, Pageable pageable) {
        return rewardRepository.findByUserIdOrderByEarnedAtDesc(userId, pageable);
    }

    public List<RewardCatalog> getCatalog() {
        return rewardCatalogRepository.findByIsActiveTrueOrderBySpinProbabilityDesc();
    }
}
