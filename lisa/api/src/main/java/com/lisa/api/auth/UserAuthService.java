    package com.lisa.api.auth;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {
    private final JdbcTemplate jdbc;
    private final PasswordHasher hasher;
    private final String defaultUsername;
    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserAuthService(JdbcTemplate jdbc, PasswordHasher hasher,
                           @Value("${LISA_USER_USERNAME:maya}") String defaultUsername,
                           @Value("${LISA_USER_PASSWORD:}") String defaultPassword,
                           @Value("${LISA_USER_CONVERSATION_ID:maya}") String conversationId) {
        this.jdbc = jdbc;
        this.hasher = hasher;
        this.defaultUsername = defaultUsername;
        if (!defaultPassword.isBlank()) ensureUser(defaultPassword, conversationId);
    }

    public LoginResult login(String username, String password) {
        return jdbc.query("SELECT id, username, password_hash, password_salt, conversation_id FROM users WHERE username = ?",
                (rs, rowNum) -> new LoginResult(rs.getLong("id"), rs.getString("username"), null,
                        hasher.matches(password, rs.getString("password_salt"), rs.getString("password_hash")),
                        rs.getString("conversation_id")), username)
                .stream().findFirst().filter(result -> result.valid()).map(user -> {
                    jdbc.update("UPDATE users SET last_login_at = NOW() WHERE id = ?", user.id());
                    String token = UUID.randomUUID() + UUID.randomUUID().toString();
                    sessions.put(token, new UserSession(user.username(), user.conversationId(), Instant.now().plusSeconds(8 * 60 * 60)));
                    return new LoginResult(user.id(), user.username(), token, true, user.conversationId());
                }).orElse(null);
    }

    @Transactional
    public LoginResult register(String username, String password) {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)", Boolean.class, username))) {
            return null;
        }
        String conversationId = "user-" + UUID.randomUUID();
        String initials = username.substring(0, Math.min(2, username.length())).toUpperCase();
        PasswordHasher.Hash hash = hasher.create(password);
        jdbc.update("INSERT INTO conversations (id, name, initials, status, topic) VALUES (?, ?, ?, 'online', 'A little check-in')",
                conversationId, username, initials);
        long userId = jdbc.queryForObject("INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, username, hash.hash(), hash.salt(), conversationId);
        String token = UUID.randomUUID() + UUID.randomUUID().toString();
        sessions.put(token, new UserSession(username, conversationId, Instant.now().plusSeconds(8 * 60 * 60)));
        return new LoginResult(userId, username, token, true, conversationId);
    }

    public UserSession session(String token) {
        UserSession session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    private void ensureUser(String password, String conversationId) {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)", Boolean.class, defaultUsername))) return;
        if (!Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM conversations WHERE id = ?)", Boolean.class, conversationId))) {
            throw new IllegalStateException("Configured user conversation does not exist");
        }
        PasswordHasher.Hash hash = hasher.create(password);
        jdbc.update("INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?)",
                defaultUsername, hash.hash(), hash.salt(), conversationId);
    }

    public record LoginResult(long id, String username, String token, boolean valid, String conversationId) { }
    public record UserSession(String username, String conversationId, Instant expiresAt) { }
}
