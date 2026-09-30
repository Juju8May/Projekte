package com.lisa.api.student.auth;

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

import com.lisa.api.auth.AuthService;
import com.lisa.api.auth.PasswordHasher;

@ExtendWith(MockitoExtension.class)
class AuthServiceStudentTest {
    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordHasher hasher;

    @Test
    void loginReturnsTokenForValidCredentials() {
        String adminUsername = "admin";
        AuthService authService = new AuthService(jdbc, hasher, adminUsername, "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class), eq(adminUsername)))
                .thenReturn(java.util.List.of(true));
        when(jdbc.update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", "admin"))
                .thenReturn(1);

        String token = authService.login(adminUsername, "secret");

        assertEquals(72, token.length());
        verify(jdbc).query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class), eq(adminUsername));
        verify(jdbc).update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", "admin");
    }

    @Test
    void loginReturnsNullForInvalidCredentials() {
        AuthService authService = new AuthService(jdbc, hasher, "admin", "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class), eq("admin")))
                .thenReturn(java.util.List.of(false));

        String token = authService.login("admin", "wrongpassword");

        verify(jdbc).query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class), eq("admin"));
        assertEquals(null, token);
    }

    @Test
    void isValidReturnsTrueForValidToken() {
        AuthService authService = new AuthService(jdbc, hasher, "admin", "");
        when(jdbc.query(
                eq("SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?"),
                any(RowMapper.class), eq("admin")))
                .thenReturn(java.util.List.of(true));

        String token = authService.login("admin", "secret");

        assertNotNull(token);
        assertTrue(authService.isValid(token));
    }
}
