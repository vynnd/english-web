package com.englishweb.backend.repository;

import com.englishweb.backend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findByIsActiveTrueOrderByDisplayOrderAsc();
}
