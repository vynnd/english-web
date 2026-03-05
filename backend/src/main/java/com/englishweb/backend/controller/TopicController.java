package com.englishweb.backend.controller;

import com.englishweb.backend.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTopics() {
        return ResponseEntity.ok(Map.of("success", true, "data", topicService.getAllActiveTopics()));
    }
}
