package com.lisa.api.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/user-auth")
public class UserAuthController {
    private final UserAuthService authService;

    public UserAuthController(UserAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        UserAuthService.LoginResult result = authService.login(request.username().trim(), request.password());
        if (result == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        return ResponseEntity.ok(Map.of("token", result.token(), "conversationId", result.conversationId(), "expiresInSeconds", 8 * 60 * 60));
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> session(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
        UserAuthService.UserSession session = authService.session(token);
        if (session == null) return ResponseEntity.ok(Map.of("authenticated", false));
        return ResponseEntity.ok(Map.of("authenticated", true, "conversationId", session.conversationId()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        String username = request.username().trim();
        UserAuthService.LoginResult result = authService.register(username, request.password());
        if (result == null) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already exists"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", result.token(), "conversationId", result.conversationId(), "expiresInSeconds", 8 * 60 * 60));
    }

    public record LoginRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(max = 200) String password) { }
}
