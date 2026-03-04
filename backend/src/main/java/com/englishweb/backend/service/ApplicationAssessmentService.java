package com.englishweb.backend.service;

import com.englishweb.backend.entity.*;
import com.englishweb.backend.exception.BadRequestException;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApplicationAssessmentService {

    private final UserVocabularyRepository userVocabularyRepository;
    private final ApplicationTaskRepository applicationTaskRepository;
    private final MissionService missionService;
    private final PointsService pointsService;

    public Map<String, Object> getStatus(UUID userId, UUID uvId) {
        UserVocabulary uv = getUv(userId, uvId);
        int level = uv.getApplicationLevel();
        if (level == 0) return Map.of("locked", true, "reason", "Word has not reached EASY grade yet");

        String taskType = getTaskTypeForLevel(level);
        return Map.of(
                "currentLevel", level,
                "levelName", getLevelName(level),
                "taskType", taskType,
                "unlockedAt", uv.getApplicationUnlockedAt()
        );
    }

    public Map<String, Object> getNextTask(UUID userId, UUID uvId) {
        UserVocabulary uv = getUv(userId, uvId);
        if (uv.getApplicationLevel() == 0) throw new BadRequestException("Word not yet unlocked for application assessment");

        int level = uv.getApplicationLevel();
        String taskType = getTaskTypeForLevel(level);
        String prompt = generatePrompt(uv.getWord(), level, taskType);

        ApplicationTask task = ApplicationTask.builder()
                .userVocabulary(uv).userId(userId)
                .level(level).taskType(taskType)
                .prompt(prompt)
                .weekReference(LocalDate.now().with(java.time.DayOfWeek.MONDAY))
                .build();
        task = applicationTaskRepository.save(task);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        result.put("level", level);
        result.put("taskType", taskType);
        result.put("prompt", prompt);
        if ("MULTIPLE_CHOICE".equals(taskType) || "CLOZE_TEST".equals(taskType)) {
            result.put("options", generateOptions(uv.getWord()));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> submitTask(UUID userId, UUID uvId, UUID taskId, String response, int responseTimeMs) {
        UserVocabulary uv = getUv(userId, uvId);
        ApplicationTask task = applicationTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        task.setUserResponse(response);
        task.setResponseTimeMs(responseTimeMs);
        task.setCompletedAt(Instant.now());

        // Evaluate response (simplified — production would use AI for LV4+)
        boolean isCorrect = evaluateResponse(task, response);
        task.setIsCorrect(isCorrect);

        if (isCorrect) {
            pointsService.addPoints(userId, 100, "APP_TASK_COMPLETE", task.getId());
            missionService.incrementAppReview(userId);
        }
        applicationTaskRepository.save(task);

        // Check level-up conditions
        boolean leveledUp = false;
        int newLevel = uv.getApplicationLevel();
        if (isCorrect && shouldLevelUp(uv, task)) {
            newLevel = Math.min(6, uv.getApplicationLevel() + 1);
            uv.setApplicationLevel(newLevel);
            userVocabularyRepository.save(uv);
            leveledUp = true;
        }

        return Map.of(
                "isCorrect", isCorrect,
                "leveledUp", leveledUp,
                "newLevel", newLevel,
                "pointsEarned", isCorrect ? 100 : 0
        );
    }

    private boolean evaluateResponse(ApplicationTask task, String response) {
        // Simplified evaluation — in production integrate AI for LV4+
        if (response == null || response.isBlank()) return false;
        // For FLASHCARD and MULTIPLE_CHOICE: check if response matches expected
        // For writing tasks: accept any non-empty response (AI would evaluate)
        return !response.isBlank() && response.length() > 2;
    }

    private boolean shouldLevelUp(UserVocabulary uv, ApplicationTask task) {
        int level = uv.getApplicationLevel();
        // Simplified: level up after 1 correct answer for LV1, 3 for LV2+
        if (level == 1) return true;
        Instant since = Instant.now().minusSeconds(30 * 24 * 3600L);
        int correctCount = applicationTaskRepository.countCorrectAtLevel(uv.getId(), level, since);
        return correctCount >= 3;
    }

    private String getTaskTypeForLevel(int level) {
        return switch (level) {
            case 1 -> "FLASHCARD";
            case 2 -> "CLOZE_TEST";
            case 3 -> "COLLOCATION";
            case 4 -> "WRITE_SENTENCE";
            case 5 -> "WRITE_PARAGRAPH";
            case 6 -> "REACTIVATION";
            default -> "FLASHCARD";
        };
    }

    private String getLevelName(int level) {
        return switch (level) {
            case 1 -> "NEW";
            case 2 -> "RECOGNIZED";
            case 3 -> "MEMORIZED";
            case 4 -> "CONTEXT_VERIFIED";
            case 5 -> "CONTROLLED_USAGE";
            case 6 -> "INTEGRATED_USAGE";
            default -> "UNKNOWN";
        };
    }

    private String generatePrompt(Word word, int level, String taskType) {
        return switch (level) {
            case 1 -> "What is the meaning of: \"" + word.getWord() + "\"?";
            case 2 -> "Fill in the blank: She felt _____ after the long journey. (" + word.getWord() + ")";
            case 3 -> "Use \"" + word.getWord() + "\" in a collocation. Write the full phrase.";
            case 4 -> "Write 2-3 sentences using \"" + word.getWord() + "\" in different contexts.";
            case 5 -> "Write a paragraph (60-80 words) that naturally includes the word \"" + word.getWord() + "\".";
            case 6 -> "Define and use \"" + word.getWord() + "\" in a sentence from memory.";
            default -> "Practice: " + word.getWord();
        };
    }

    private List<String> generateOptions(Word word) {
        // Simplified — production would fetch distractors from word dictionary
        return List.of(word.getWord(), "option_b", "option_c", "option_d");
    }

    private UserVocabulary getUv(UUID userId, UUID uvId) {
        UserVocabulary uv = userVocabularyRepository.findById(uvId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary entry not found"));
        if (!uv.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Vocabulary entry not found");
        return uv;
    }
}
