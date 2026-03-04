package com.englishweb.backend.service;

import com.englishweb.backend.entity.User;
import com.englishweb.backend.exception.BadRequestException;
import com.englishweb.backend.repository.UserRepository;
import com.englishweb.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> register(String email, String username, String password) {
        if (userRepository.existsByEmail(email)) throw new BadRequestException("Email already in use");
        if (userRepository.existsByUsername(username)) throw new BadRequestException("Username already taken");

        User user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new BadRequestException("Invalid email or password");

        return buildTokenResponse(user);
    }

    public Map<String, Object> refresh(String refreshToken) {
        if (!jwtUtil.isValid(refreshToken)) throw new BadRequestException("Invalid refresh token");
        UUID userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return Map.of("accessToken", jwtUtil.generateAccessToken(user.getId(), user.getEmail()));
    }

    private Map<String, Object> buildTokenResponse(User user) {
        return Map.of(
                "accessToken", jwtUtil.generateAccessToken(user.getId(), user.getEmail()),
                "refreshToken", jwtUtil.generateRefreshToken(user.getId()),
                "user", Map.of("id", user.getId(), "email", user.getEmail(), "username", user.getUsername())
        );
    }
}
