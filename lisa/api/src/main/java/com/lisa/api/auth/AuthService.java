package com.lisa.api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final JdbcTemplate jdbc;
    private final PasswordHasher hasher;
    private final String adminUsername;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(JdbcTemplate jdbc, PasswordHasher hasher,
                       @Value("${LISA_ADMIN_USERNAME:admin}") String adminUsername,
                       @Value("${LISA_ADMIN_PASSWORD:}") String adminPassword) {
        this.jdbc = jdbc;
        this.hasher = hasher;
        this.adminUsername = adminUsername;
        if (!adminPassword.isBlank()) ensureAdmin(adminPassword);
    }

    public String login(String username, String password) {
        return jdbc.query("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?",
                (rs, rowNum) -> hasher.matches(password, rs.getString("password_salt"), rs.getString("password_hash")), username)
                .stream().findFirst().filter(Boolean::booleanValue).map(valid -> {
                    jdbc.update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", username);
                    String token = UUID.randomUUID() + UUID.randomUUID().toString();
                    sessions.put(token, new Session(username, Instant.now().plusSeconds(8 * 60 * 60)));
                    return token;
                }).orElse(null);
    }

    public boolean isValid(String token) {
        Session session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    private void ensureAdmin(String password) {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM admin_users WHERE username = ?)", Boolean.class, adminUsername))) return;
        PasswordHasher.Hash hash = hasher.create(password);
        jdbc.update("INSERT INTO admin_users (username, password_hash, password_salt, role) VALUES (?, ?, ?, 'ADMIN')",
                adminUsername, hash.hash(), hash.salt());
    }

    private record Session(String username, Instant expiresAt) { }
}
