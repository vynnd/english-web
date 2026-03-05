package com.englishweb.backend.service;

import com.englishweb.backend.entity.User;
import com.englishweb.backend.entity.UserPointsLog;
import com.englishweb.backend.repository.UserPointsLogRepository;
import com.englishweb.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PointsService {

    private final UserRepository userRepository;
    private final UserPointsLogRepository userPointsLogRepository;

    @Autowired
    public PointsService(UserRepository userRepository, UserPointsLogRepository userPointsLogRepository) {
        this.userRepository = userRepository;
        this.userPointsLogRepository = userPointsLogRepository;
    }

    @Transactional
    public void addPoints(UUID userId, int points, String reason, UUID referenceId) {
        User user = userRepository.findById(userId).orElseThrow();
        int newTotal = user.getTotalPoints() + points;
        user.setTotalPoints(newTotal);
        updateTier(user, newTotal);
        userRepository.save(user);

        UserPointsLog log = UserPointsLog.builder()
                .user(user).pointsDelta(points).reason(reason)
                .referenceId(referenceId).totalAfter(newTotal).build();
        userPointsLogRepository.save(log);
    }

    private void updateTier(User user, int points) {
        if (points >= 10000) user.setTier("ACHIEVEMENT");
        else if (points >= 5000) user.setTier("PERSISTENT");
        else if (points >= 3000) user.setTier("EFFORT");
        else if (points >= 2000) user.setTier("TRYING");
        else if (points >= 1000) user.setTier("BEGINNER");
    }

    public Page<UserPointsLog> getHistory(UUID userId, Pageable pageable) {
        return userPointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
