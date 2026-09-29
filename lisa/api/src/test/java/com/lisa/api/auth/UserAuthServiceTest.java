package com.lisa.api.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {
    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordHasher hasher;

    @Test
    void constructorCreatesConfiguredUserWhenUserAndConversationExistenceChecksPass() {
        PasswordHasher.Hash hash = new PasswordHasher.Hash("salt", "hash");
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)", Boolean.class, "maya"))
                .thenReturn(false);
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM conversations WHERE id = ?)", Boolean.class, "maya"))
                .thenReturn(true);
        when(hasher.create("secret")).thenReturn(hash);
        when(jdbc.update(
                "INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?)",
                "maya", "hash", "salt", "maya"))
                .thenReturn(1);

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "secret", "maya");

        assertNotNull(service);
        verify(jdbc).update(
                "INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?)",
                "maya", "hash", "salt", "maya");
    }

    @Test
    void registrationCreatesNewConversationAndReturnsValidSession() {
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)", Boolean.class, "newuser"))
                .thenReturn(false);
        when(hasher.create("secret")).thenReturn(new PasswordHasher.Hash("salt", "hash"));
        when(jdbc.queryForObject(
                eq("INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?) RETURNING id"),
                eq(Long.class), eq("newuser"), eq("hash"), eq("salt"), any(String.class)))
                .thenReturn(12L);

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");
        UserAuthService.LoginResult result = service.register("newuser", "secret");

        assertNotNull(result);
        assertTrue(result.valid());
        assertTrue(result.conversationId().startsWith("user-"));
        assertNotNull(service.session(result.token()));
        verify(jdbc).update(
                eq("INSERT INTO conversations (id, name, initials, status, topic) VALUES (?, ?, ?, 'online', 'A little check-in')"),
                eq(result.conversationId()), eq("newuser"), eq("NE"));
    }
}
