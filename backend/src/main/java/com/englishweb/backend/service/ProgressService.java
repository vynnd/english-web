package com.englishweb.backend.service;

import com.englishweb.backend.entity.DailyProgress;
import com.englishweb.backend.entity.User;
import com.englishweb.backend.entity.UserGoal;
import com.englishweb.backend.repository.DailyProgressRepository;
import com.englishweb.backend.repository.UserGoalRepository;
import com.englishweb.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProgressService {

    private final DailyProgressRepository dailyProgressRepository;
    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    @Autowired
    public ProgressService(DailyProgressRepository dailyProgressRepository,
                           UserRepository userRepository,
                           UserGoalRepository userGoalRepository) {
        this.dailyProgressRepository = dailyProgressRepository;
        this.userRepository = userRepository;
        this.userGoalRepository = userGoalRepository;
    }

    public DailyProgress getTodayProgress(UUID userId) {
        return dailyProgressRepository.findByUserIdAndDate(userId, LocalDate.now())
                .orElseGet(() -> DailyProgress.builder()
                        .date(LocalDate.now())
                        .readingSeconds(0).readingGoalAchieved(false)
                        .wordsSaved(0).wordsReachedEasy(0).vocabGoalAchieved(false)
                        .reviewsCompleted(0).reviewGoalAchieved(false)
                        .streakMaintained(false).streakCount(0).build());
    }

    @Transactional
    public void addReadingSeconds(UUID userId, int seconds) {
        DailyProgress progress = getOrCreate(userId);
        UserGoal goal = userGoalRepository.findCurrentGoal(userId, LocalDate.now())
                .orElse(null);
        int goalSeconds = goal != null ? goal.getReadingMinutesGoal() * 60 : 600;

        progress.setReadingSeconds(progress.getReadingSeconds() + seconds);
        if (!progress.getReadingGoalAchieved() && progress.getReadingSeconds() >= goalSeconds) {
            progress.setReadingGoalAchieved(true);
        }
        checkAndUpdateStreak(userId, progress);
        dailyProgressRepository.save(progress);
    }

    @Transactional
    public void addWordsSaved(UUID userId, int count) {
        DailyProgress progress = getOrCreate(userId);
        progress.setWordsSaved(progress.getWordsSaved() + count);
        dailyProgressRepository.save(progress);
    }

    @Transactional
    public void addWordReachedEasy(UUID userId) {
        DailyProgress progress = getOrCreate(userId);
        UserGoal goal = userGoalRepository.findCurrentGoal(userId, LocalDate.now()).orElse(null);
        int vocabGoal = goal != null ? goal.getVocabCountGoal() : 5;

        progress.setWordsReachedEasy(progress.getWordsReachedEasy() + 1);
        if (!progress.getVocabGoalAchieved() && progress.getWordsReachedEasy() >= vocabGoal) {
            progress.setVocabGoalAchieved(true);
        }
        // Review goal: 50% of vocab goal
        int reviewGoal = Math.max(1, vocabGoal / 2);
        if (progress.getReviewsCompleted() >= reviewGoal) progress.setReviewGoalAchieved(true);

        checkAndUpdateStreak(userId, progress);
        dailyProgressRepository.save(progress);
    }

    @Transactional
    public void addReviewCompleted(UUID userId) {
        DailyProgress progress = getOrCreate(userId);
        UserGoal goal = userGoalRepository.findCurrentGoal(userId, LocalDate.now()).orElse(null);
        int reviewGoal = goal != null ? Math.max(1, goal.getVocabCountGoal() / 2) : 3;

        progress.setReviewsCompleted(progress.getReviewsCompleted() + 1);
        if (!progress.getReviewGoalAchieved() && progress.getReviewsCompleted() >= reviewGoal) {
            progress.setReviewGoalAchieved(true);
        }
        checkAndUpdateStreak(userId, progress);
        dailyProgressRepository.save(progress);
    }

    private void checkAndUpdateStreak(UUID userId, DailyProgress progress) {
        if (progress.getReadingGoalAchieved() && progress.getVocabGoalAchieved() && progress.getReviewGoalAchieved()
                && !progress.getStreakMaintained()) {
            progress.setStreakMaintained(true);
            // Update user streak
            userRepository.findById(userId).ifPresent(user -> {
                int newStreak = user.getCurrentStreak() + 1;
                user.setCurrentStreak(newStreak);
                if (newStreak > user.getLongestStreak()) user.setLongestStreak(newStreak);
                userRepository.save(user);
                progress.setStreakCount(newStreak);
            });
        }
    }

    public Map<String, Object> getStreak(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return Map.of("currentStreak", user.getCurrentStreak(), "longestStreak", user.getLongestStreak());
    }

    public List<DailyProgress> getHistory(UUID userId, LocalDate from, LocalDate to) {
        return dailyProgressRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, from, to);
    }

    private DailyProgress getOrCreate(UUID userId) {
        return dailyProgressRepository.findByUserIdAndDate(userId, LocalDate.now())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    DailyProgress p = DailyProgress.builder()
                            .user(user).date(LocalDate.now())
                            .readingSeconds(0).readingGoalAchieved(false)
                            .wordsSaved(0).wordsReachedEasy(0).vocabGoalAchieved(false)
                            .reviewsCompleted(0).reviewGoalAchieved(false)
                            .streakMaintained(false).streakCount(user.getCurrentStreak()).build();
                    return dailyProgressRepository.save(p);
                });
    }
}
