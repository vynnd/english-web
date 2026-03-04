package com.englishweb.backend.service;

import com.englishweb.backend.entity.Topic;
import com.englishweb.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public List<Topic> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
}
