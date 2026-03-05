package com.englishweb.backend.service;

import com.englishweb.backend.entity.Topic;
import com.englishweb.backend.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    @Autowired
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
}
