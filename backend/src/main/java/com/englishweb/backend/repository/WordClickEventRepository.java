package com.englishweb.backend.repository;

import com.englishweb.backend.entity.WordClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WordClickEventRepository extends JpaRepository<WordClickEvent, UUID> {
    @Query("SELECT e FROM WordClickEvent e JOIN FETCH e.word WHERE e.readingSession.id = :sessionId AND e.user.id = :userId")
    List<WordClickEvent> findBySessionAndUser(UUID sessionId, UUID userId);

    void deleteByReadingSessionId(UUID sessionId);
}
