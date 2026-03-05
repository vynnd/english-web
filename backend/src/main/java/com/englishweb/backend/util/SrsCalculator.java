package com.englishweb.backend.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SrsCalculator {

    public enum Grade { EASY, GOOD, HARD, AGAIN }

    public Grade calculateGrade(int responseTimeMs) {
        if (responseTimeMs < 3000) return Grade.EASY;
        if (responseTimeMs < 5000) return Grade.GOOD;
        if (responseTimeMs < 10000) return Grade.HARD;
        return Grade.AGAIN;
    }

    /**
     * Calculate next interval in minutes based on current phase.
     * LEARNING: fixed steps. REVIEW: multiplier-based.
     */
    public int calculateNextInterval(String phase, int currentIntervalMinutes, Grade grade) {
        if ("LEARNING".equals(phase)) {
            return switch (grade) {
                case EASY -> 1440;  // 1 day — skip to review
                case GOOD -> 60;    // 1 hour
                case HARD -> 10;    // 10 minutes
                case AGAIN -> 5;    // 5 minutes
            };
        }
        // REVIEW phase — multiplier based
        double multiplier = switch (grade) {
            case EASY -> 2.0;
            case GOOD -> 1.5;
            case HARD -> 0.7;
            case AGAIN -> 0.3;
        };
        int next = (int) Math.round(currentIntervalMinutes * multiplier);
        return grade == Grade.AGAIN ? 10 : Math.max(next, 1);
    }

    /**
     * Determine next memory state.
     */
    public String calculateNextMemoryState(String currentState, String phase, Grade grade) {
        if (grade == Grade.AGAIN) {
            return "RELEARNING".equals(currentState) ? "RELEARNING" : "LEARNING";
        }
        if ("LEARNING".equals(phase)) {
            // After EASY in learning phase → jump to REVIEW
            if (grade == Grade.EASY) return "REVIEW";
            // Still in learning steps
            return "LEARNING";
        }
        // REVIEW phase
        if (grade == Grade.EASY && currentState.equals("REVIEW")) {
            // Could check interval threshold for MASTERED here
            return "REVIEW";
        }
        return "REVIEW";
    }

    public String calculateNextPhase(String currentPhase, Grade grade) {
        if ("LEARNING".equals(currentPhase) && grade == Grade.EASY) return "REVIEW";
        if (grade == Grade.AGAIN) return "LEARNING";
        return currentPhase;
    }

    public Instant calculateDueAt(int intervalMinutes) {
        return Instant.now().plus(intervalMinutes, ChronoUnit.MINUTES);
    }
}
