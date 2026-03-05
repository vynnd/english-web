package com.englishweb.backend.repository;

import com.englishweb.backend.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WordRepository extends JpaRepository<Word, UUID> {
    Optional<Word> findByWordIgnoreCase(String word);
}
