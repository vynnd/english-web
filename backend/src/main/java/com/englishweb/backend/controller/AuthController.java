package com.englishweb.backend.controller;

import com.englishweb.backend.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    record RegisterRequest(@Email @NotBlank String email,
                           @NotBlank @Size(min=3, max=50) String username,
                           @NotBlank @Size(min=6) String password) {}

    record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    record RefreshRequest(@NotBlank String refreshToken) {}

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", authService.register(req.email(), req.username(), req.password())));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", authService.login(req.email(), req.password())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", authService.refresh(req.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // Stateless JWT — client discards token
        return ResponseEntity.ok(Map.of("success", true, "data", "Logged out"));
    }
}
