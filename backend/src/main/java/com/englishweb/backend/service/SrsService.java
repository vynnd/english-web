package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.*;
import com.englishweb.backend.util.SrsCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class SrsService {

    private final UserVocabularyRepository userVocabularyRepository;
    private final SrsReviewRepository srsReviewRepository;
    private final SrsCalculator srsCalculator;
    private final ProgressService progressService;
    private final MissionService missionService;
    private final PointsService pointsService;

    @Autowired
    public SrsService(UserVocabularyRepository userVocabularyRepository,
                      SrsReviewRepository srsReviewRepository,
                      SrsCalculator srsCalculator,
                      ProgressService progressService,
                      MissionService missionService,
                      PointsService pointsService) {
        this.userVocabularyRepository = userVocabularyRepository;
        this.srsReviewRepository = srsReviewRepository;
        this.srsCalculator = srsCalculator;
        this.progressService = progressService;
        this.missionService = missionService;
        this.pointsService = pointsService;
    }

    @Transactional
    public Map<String, Object> submitReview(UUID userId, UUID userVocabularyId, int responseTimeMs) {
        UserVocabulary uv = userVocabularyRepository.findById(userVocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary entry not found"));
        if (!uv.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Vocabulary entry not found");

        SrsCalculator.Grade grade = srsCalculator.calculateGrade(responseTimeMs);
        String phase = uv.getSrsIntervalMinutes() < 1440 ? "LEARNING" : "REVIEW";
        int prevInterval = uv.getSrsIntervalMinutes();
        int newInterval = srsCalculator.calculateNextInterval(phase, prevInterval, grade);
        String newState = srsCalculator.calculateNextMemoryState(uv.getMemoryState(), phase, grade);
        String newPhase = srsCalculator.calculateNextPhase(phase, grade);
        Instant dueAt = srsCalculator.calculateDueAt(newInterval);

        // Save review log
        SrsReview review = SrsReview.builder()
                .userVocabulary(uv).userId(userId)
                .responseTimeMs(responseTimeMs)
                .grade(grade.name()).phase(phase)
                .previousIntervalMinutes(prevInterval)
                .newIntervalMinutes(newInterval)
                .build();
        srsReviewRepository.save(review);

        // Update vocabulary
        uv.setMemoryState(newState);
        uv.setSrsIntervalMinutes(newInterval);
        uv.setSrsDueAt(dueAt);
        uv.setReviewCount(uv.getReviewCount() + 1);
        uv.setLastReviewedAt(Instant.now());

        int pointsEarned = 0;

        // Unlock application if first time EASY
        if (grade == SrsCalculator.Grade.EASY && uv.getApplicationLevel() == 0) {
            uv.setApplicationLevel(1);
            uv.setApplicationUnlockedAt(Instant.now());
            pointsEarned += 50;
            pointsService.addPoints(userId, 50, "WORD_EASY", uv.getId());
            progressService.addWordReachedEasy(userId);
            missionService.incrementEasyWords(userId);
        }

        userVocabularyRepository.save(uv);
        progressService.addReviewCompleted(userId);

        return Map.of(
                "grade", grade.name(),
                "nextReviewAt", dueAt,
                "newMemoryState", newState,
                "nextPhase", newPhase,
                "pointsEarned", pointsEarned
        );
    }
}
