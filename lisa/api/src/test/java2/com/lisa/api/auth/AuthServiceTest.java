package com.lisa.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordHasher hasher;

    private AuthService authService;

    @Test
    void loginReturnsTokenForValidCredentials() {
        String adminUsername = "admin";
        String adminPassword = "secret";
        authService = new AuthService(jdbc, hasher, adminUsername, "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class),
                eq(adminUsername)))
                .thenReturn(java.util.List.of(true));
        when(jdbc.update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", "admin")).thenReturn(1);

        String token = authService.login(adminUsername, adminPassword);

        assertEquals(72, token.length());
        verify(jdbc).query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class),
                eq(adminUsername));
        verify(jdbc).update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", "admin");
    }

    @Test
    void loginReturnsNullForInvalidCredentials() {
        String adminUsername = "admin";
        String adminPassword = "wrongpassword";
        authService = new AuthService(jdbc, hasher, adminUsername, "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class),
                eq(adminUsername)))
                .thenReturn(java.util.List.of(false));
        String token = authService.login(adminUsername, adminPassword);

        verify(jdbc).query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class),
                eq(adminUsername));
        assertEquals(null, token);
    }

    @Test
    void isValidReturnsTrueForValidToken() {
        authService = new AuthService(jdbc, hasher, "admin", "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class),
                eq("admin")))
                .thenReturn(java.util.List.of(true));

        String token = authService.login("admin", "secret");

        assertNotNull(token);
        assertTrue(authService.isValid(token));
    }


}
