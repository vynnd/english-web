package com.englishweb.backend.repository;

import com.englishweb.backend.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface UserGoalRepository extends JpaRepository<UserGoal, UUID> {
    @Query("SELECT g FROM UserGoal g WHERE g.user.id = :userId AND g.effectiveFrom <= :date ORDER BY g.effectiveFrom DESC LIMIT 1")
    Optional<UserGoal> findCurrentGoal(UUID userId, LocalDate date);
}
