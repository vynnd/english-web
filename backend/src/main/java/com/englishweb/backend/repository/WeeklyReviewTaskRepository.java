package com.englishweb.backend.repository;

import com.englishweb.backend.entity.WeeklyReviewTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WeeklyReviewTaskRepository extends JpaRepository<WeeklyReviewTask, UUID> {
    List<WeeklyReviewTask> findByWeeklyReviewSetId(UUID setId);
}
