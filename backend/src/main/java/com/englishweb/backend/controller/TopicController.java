package com.englishweb.backend.controller;

import com.englishweb.backend.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTopics() {
        return ResponseEntity.ok(Map.of("success", true, "data", topicService.getAllActiveTopics()));
    }
}
