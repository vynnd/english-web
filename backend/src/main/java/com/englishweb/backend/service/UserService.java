package com.englishweb.backend.service;

import com.englishweb.backend.entity.User;
import com.englishweb.backend.entity.UserGoal;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.UserGoalRepository;
import com.englishweb.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserGoalRepository userGoalRepository) {
        this.userRepository = userRepository;
        this.userGoalRepository = userGoalRepository;
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User upgradeToPremium(UUID userId) {
        User user = getUser(userId);
        user.setIsPremium(true);
        user.setDailyWordLimit(20);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUsername(UUID userId, String username) {
        User user = getUser(userId);
        user.setUsername(username);
        return userRepository.save(user);
    }

    public UserGoal getCurrentGoal(UUID userId) {
        return userGoalRepository.findCurrentGoal(userId, LocalDate.now())
                .orElseGet(() -> UserGoal.builder()
                        .readingMinutesGoal(10).vocabCountGoal(5).effectiveFrom(LocalDate.now()).build());
    }

    @Transactional
    public UserGoal updateGoal(UUID userId, int readingMinutes, int vocabCount) {
        User user = getUser(userId);
        UserGoal goal = UserGoal.builder()
                .user(user)
                .readingMinutesGoal(readingMinutes)
                .vocabCountGoal(vocabCount)
                .effectiveFrom(LocalDate.now())
                .build();
        return userGoalRepository.save(goal);
    }
}
