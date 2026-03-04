package com.englishweb.backend.controller;

import com.englishweb.backend.service.ApplicationAssessmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vocabulary/{id}/application")
@RequiredArgsConstructor
public class ApplicationAssessmentController {

    private final ApplicationAssessmentService applicationAssessmentService;

    record SubmitRequest(@NotNull UUID taskId, @NotBlank String response, int responseTimeMs) {}

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@AuthenticationPrincipal UserDetails ud,
                                                          @PathVariable UUID id) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(applicationAssessmentService.getStatus(userId, id));
    }

    @GetMapping("/task")
    public ResponseEntity<Map<String, Object>> getNextTask(@AuthenticationPrincipal UserDetails ud,
                                                            @PathVariable UUID id) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(applicationAssessmentService.getNextTask(userId, id));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitTask(@AuthenticationPrincipal UserDetails ud,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody SubmitRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ok(applicationAssessmentService.submitTask(userId, id, req.taskId(), req.response(), req.responseTimeMs()));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
