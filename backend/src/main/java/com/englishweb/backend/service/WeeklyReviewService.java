package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.BadRequestException;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class WeeklyReviewService {

    private final WeeklyReviewSetRepository weeklyReviewSetRepository;
    private final WeeklyReviewTaskRepository weeklyReviewTaskRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public WeeklyReviewService(WeeklyReviewSetRepository weeklyReviewSetRepository,
                                WeeklyReviewTaskRepository weeklyReviewTaskRepository,
                                UserVocabularyRepository userVocabularyRepository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.weeklyReviewSetRepository = weeklyReviewSetRepository;
        this.weeklyReviewTaskRepository = weeklyReviewTaskRepository;
        this.userVocabularyRepository = userVocabularyRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getCurrentWeekReview(UUID userId) {
        int activeLv2Plus = userVocabularyRepository.countActiveLv2Plus(userId);
        if (activeLv2Plus < 3) {
            return Map.of("locked", true, "reason", "Need at least 3 words at LV2+ to unlock weekly review",
                    "activeLv2Plus", activeLv2Plus);
        }

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyReviewSet set = weeklyReviewSetRepository.findByUserIdAndWeekStart(userId, weekStart)
                .orElseGet(() -> generateWeeklySet(userId, weekStart));

        List<WeeklyReviewTask> tasks = weeklyReviewTaskRepository.findByWeeklyReviewSetId(set.getId());
        long completedCount = tasks.stream().filter(WeeklyReviewTask::getIsCompleted).count();

        return Map.of(
                "id", set.getId(),
                "weekStart", set.getWeekStart(),
                "selectedWords", set.getSelectedWords(),
                "fixedReward", set.getFixedReward() != null ? set.getFixedReward() : "{}",
                "isCompleted", set.getIsCompleted(),
                "completedCount", completedCount,
                "totalCount", tasks.size()
        );
    }

    public List<WeeklyReviewTask> getTasks(UUID userId) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyReviewSet set = weeklyReviewSetRepository.findByUserIdAndWeekStart(userId, weekStart)
                .orElseThrow(() -> new ResourceNotFoundException("No weekly review for this week"));
        return weeklyReviewTaskRepository.findByWeeklyReviewSetId(set.getId());
    }

    @Transactional
    public WeeklyReviewTask completeTask(UUID userId, UUID taskId, UUID applicationTaskId) {
        WeeklyReviewTask task = weeklyReviewTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getWeeklyReviewSet().getUser().getId().equals(userId))
            throw new ResourceNotFoundException("Task not found");
        if (task.getIsCompleted()) throw new BadRequestException("Task already completed");

        task.setIsCompleted(true);
        task.setCompletedAt(Instant.now());
        return weeklyReviewTaskRepository.save(task);
    }

    @Transactional
    private WeeklyReviewSet generateWeeklySet(UUID userId, LocalDate weekStart) {
        User user = userRepository.findById(userId).orElseThrow();

        // Select 6 words by 4 priority groups
        Set<UUID> selected = new LinkedHashSet<>();
        List<Map<String, Object>> selectedWords = new ArrayList<>();

        addWords(selectedWords, selected, userVocabularyRepository.findNearLevelUp(userId), "NEAR_LEVELUP", 2);
        addWords(selectedWords, selected, userVocabularyRepository.findNearDue(userId), "NEAR_DUE", 2);
        userVocabularyRepository.findHighestLevel(userId)
                .filter(uv -> selected.add(uv.getId()))
                .ifPresent(uv -> selectedWords.add(wordEntry(uv, "HIGHEST_LEVEL")));
        userVocabularyRepository.findLeastRecentlyUsed(userId)
                .filter(uv -> selected.add(uv.getId()))
                .ifPresent(uv -> selectedWords.add(wordEntry(uv, "UNUSED")));

        String selectedJson;
        try { selectedJson = objectMapper.writeValueAsString(selectedWords); }
        catch (Exception e) { selectedJson = "[]"; }

        WeeklyReviewSet set = WeeklyReviewSet.builder()
                .user(user).weekStart(weekStart)
                .selectedWords(selectedJson)
                .fixedReward("{\"description\":\"Bonus XP\",\"value\":200}")
                .isCompleted(false).build();
        set = weeklyReviewSetRepository.save(set);

        // Create review tasks
        for (Map<String, Object> w : selectedWords) {
            UUID uvId = (UUID) w.get("userVocabularyId");
            UserVocabulary uv = userVocabularyRepository.findById(uvId).orElse(null);
            if (uv == null) continue;
            WeeklyReviewTask task = WeeklyReviewTask.builder()
                    .weeklyReviewSet(set).userVocabulary(uv).isCompleted(false).build();
            weeklyReviewTaskRepository.save(task);
        }
        return set;
    }

    private void addWords(List<Map<String, Object>> result, Set<UUID> seen,
                          List<UserVocabulary> candidates, String reason, int max) {
        int added = 0;
        for (UserVocabulary uv : candidates) {
            if (added >= max) break;
            if (seen.add(uv.getId())) {
                result.add(wordEntry(uv, reason));
                added++;
            }
        }
    }

    private Map<String, Object> wordEntry(UserVocabulary uv, String reason) {
        return Map.of(
                "userVocabularyId", uv.getId(),
                "wordId", uv.getWord().getId(),
                "word", uv.getWord().getWord(),
                "selectionReason", reason
        );
    }
}
