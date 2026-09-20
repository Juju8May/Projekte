package com.lisa.api.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceBehaviorTest {
    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordHasher hasher;

    @Test
    void loginStoresSessionForValidCredentials() throws SQLException {
        when(hasher.matches("secret", "salt", "hash")).thenReturn(true);
        when(jdbc.query(
                eq("SELECT id, username, password_hash, password_salt, conversation_id FROM users WHERE username = ?"),
                any(RowMapper.class),
                eq("maya")))
                .thenAnswer(invocation -> List.of(mapLoginResult(invocation.getArgument(1))));
        when(jdbc.update("UPDATE users SET last_login_at = NOW() WHERE id = ?", 7L)).thenReturn(1);

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");
        UserAuthService.LoginResult result = service.login("maya", "secret");

        assertNotNull(result);
        UserAuthService.UserSession session = service.session(result.token());
        assertNotNull(session);
        assertTrue(session.username().equals("maya"));
        assertTrue(session.conversationId().equals("conversation-1"));
    }

    @Test
    void loginReturnsNullForInvalidCredentials() throws SQLException {
        when(hasher.matches("wrong", "salt", "hash")).thenReturn(false);
        when(jdbc.query(
                eq("SELECT id, username, password_hash, password_salt, conversation_id FROM users WHERE username = ?"),
                any(RowMapper.class),
                eq("maya")))
                .thenAnswer(invocation -> List.of(mapLoginResult(invocation.getArgument(1))));

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");

        assertNull(service.login("maya", "wrong"));
    }

    @Test
    void sessionReturnsNullForUnknownToken() {
        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");

        assertNull(service.session("unknown-token"));
    }

    @Test
    void registerCreatesUserAndStoresSession() {
        PasswordHasher.Hash hash = new PasswordHasher.Hash("salt", "hash");
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)",
                Boolean.class,
                "newuser"))
                .thenReturn(false);
        when(hasher.create("secret")).thenReturn(hash);
        when(jdbc.queryForObject(
                eq("INSERT INTO users (username, password_hash, password_salt, conversation_id) VALUES (?, ?, ?, ?) RETURNING id"),
                eq(Long.class),
                eq("newuser"), eq("hash"), eq("salt"), anyString()))
                .thenReturn(12L);

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");
        UserAuthService.LoginResult result = service.register("newuser", "secret");

        assertNotNull(result);
        assertTrue(result.valid());
        assertTrue(result.token() != null && !result.token().isBlank());
        assertNotNull(service.session(result.token()));
    }

    @Test
    void registerReturnsNullWhenUsernameAlreadyExists() {
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)",
                Boolean.class,
                "existing"))
                .thenReturn(true);

        UserAuthService service = new UserAuthService(jdbc, hasher, "maya", "", "maya");

        assertNull(service.register("existing", "secret"));
    }

    private UserAuthService.LoginResult mapLoginResult(RowMapper<UserAuthService.LoginResult> mapper)
            throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(7L);
        when(resultSet.getString("username")).thenReturn("maya");
        when(resultSet.getString("password_salt")).thenReturn("salt");
        when(resultSet.getString("password_hash")).thenReturn("hash");
        when(resultSet.getString("conversation_id")).thenReturn("conversation-1");
        return mapper.mapRow(resultSet, 0);
    }
}
