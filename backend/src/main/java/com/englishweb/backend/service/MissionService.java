package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.BadRequestException;
import com.englishweb.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.UUID;

@Service
public class MissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private final WeeklyMissionRepository weeklyMissionRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    @Autowired
    public MissionService(DailyMissionRepository dailyMissionRepository,
                          WeeklyMissionRepository weeklyMissionRepository,
                          UserRepository userRepository,
                          RewardService rewardService) {
        this.dailyMissionRepository = dailyMissionRepository;
        this.weeklyMissionRepository = weeklyMissionRepository;
        this.userRepository = userRepository;
        this.rewardService = rewardService;
    }

    public DailyMission getTodayMission(UUID userId) {
        return dailyMissionRepository.findByUserIdAndDate(userId, LocalDate.now())
                .orElseGet(() -> createDailyMission(userId));
    }

    @Transactional
    public DailyMission createDailyMission(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        DailyMission mission = DailyMission.builder()
                .user(user).date(LocalDate.now())
                .easyWordsTarget(5).easyWordsAchieved(0)
                .appReviewTarget(1).appReviewAchieved(0)
                .isCompleted(false).build();
        return dailyMissionRepository.save(mission);
    }

    @Transactional
    public void incrementEasyWords(UUID userId) {
        DailyMission mission = getTodayMission(userId);
        mission.setEasyWordsAchieved(mission.getEasyWordsAchieved() + 1);
        checkDailyCompletion(mission);
        dailyMissionRepository.save(mission);

        WeeklyMission weekly = getThisWeekMission(userId);
        weekly.setEasyWordsAchieved(weekly.getEasyWordsAchieved() + 1);
        checkWeeklyCompletion(weekly);
        weeklyMissionRepository.save(weekly);
    }

    @Transactional
    public void incrementAppReview(UUID userId) {
        DailyMission mission = getTodayMission(userId);
        mission.setAppReviewAchieved(mission.getAppReviewAchieved() + 1);
        checkDailyCompletion(mission);
        dailyMissionRepository.save(mission);

        WeeklyMission weekly = getThisWeekMission(userId);
        weekly.setAppReviewAchieved(weekly.getAppReviewAchieved() + 1);
        checkWeeklyCompletion(weekly);
        weeklyMissionRepository.save(weekly);
    }

    private void checkDailyCompletion(DailyMission mission) {
        if (!mission.getIsCompleted()
                && mission.getEasyWordsAchieved() >= mission.getEasyWordsTarget()
                && mission.getAppReviewAchieved() >= mission.getAppReviewTarget()) {
            mission.setIsCompleted(true);
            mission.setCompletedAt(Instant.now());
        }
    }

    private void checkWeeklyCompletion(WeeklyMission mission) {
        if (!mission.getIsCompleted()
                && mission.getEasyWordsAchieved() >= mission.getEasyWordsTarget()
                && mission.getAppReviewAchieved() >= mission.getAppReviewTarget()) {
            mission.setIsCompleted(true);
            mission.setCompletedAt(Instant.now());
        }
    }

    @Transactional
    public Reward claimDailySpin(UUID userId) {
        DailyMission mission = getTodayMission(userId);
        if (!mission.getIsCompleted()) throw new BadRequestException("Daily mission not completed yet");
        if (mission.getSpinReward() != null) throw new BadRequestException("Spin already claimed today");

        Reward reward = rewardService.grantSpin(userId);
        mission.setSpinReward(reward);
        dailyMissionRepository.save(mission);
        return reward;
    }

    public WeeklyMission getThisWeekMission(UUID userId) {
        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return weeklyMissionRepository.findByUserIdAndWeekStart(userId, weekStart)
                .orElseGet(() -> createWeeklyMission(userId, weekStart));
    }

    @Transactional
    public WeeklyMission createWeeklyMission(UUID userId, LocalDate weekStart) {
        User user = userRepository.findById(userId).orElseThrow();
        WeeklyMission mission = WeeklyMission.builder()
                .user(user).weekStart(weekStart)
                .easyWordsTarget(20).easyWordsAchieved(0)
                .appReviewTarget(6).appReviewAchieved(0)
                .isCompleted(false).build();
        return weeklyMissionRepository.save(mission);
    }

    @Transactional
    public Reward claimWeeklyDice(UUID userId) {
        WeeklyMission mission = getThisWeekMission(userId);
        if (!mission.getIsCompleted()) throw new BadRequestException("Weekly mission not completed yet");
        if (mission.getDiceReward() != null) throw new BadRequestException("Dice reward already claimed this week");

        int diceCount = (int) (Math.random() * 3) + 1; // 1-3 dice
        Reward reward = rewardService.grantDice(userId, diceCount);
        mission.setDiceCount(diceCount);
        mission.setDiceReward(reward);
        weeklyMissionRepository.save(mission);
        return reward;
    }
}
