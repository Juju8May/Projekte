package com.lisa.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
class AuthServiceTest {
    private static final String FIND_ADMIN_SQL =
            "SELECT username, password_hash, password_salt FROM admin_users WHERE username = ?";

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordHasher hasher;

    @Test
    void loginReturnsTokenAndUpdatesLastLoginForValidCredentials() {
        AuthService service = new AuthService(jdbc, hasher, "admin", "");
        when(jdbc.query(eq(FIND_ADMIN_SQL), any(RowMapper.class), eq("admin")))
                .thenReturn(java.util.List.of(true));

        String token = service.login("admin", "secret");

        assertNotNull(token);
        assertEquals(72, token.length());
        assertTrue(service.isValid(token));
        verify(jdbc).update("UPDATE admin_users SET last_login_at = NOW() WHERE username = ?", "admin");
    }

    @Test
    void loginReturnsNullForInvalidCredentials() {
        AuthService service = new AuthService(jdbc, hasher, "admin", "");
        when(jdbc.query(eq(FIND_ADMIN_SQL), any(RowMapper.class), eq("admin")))
                .thenReturn(java.util.List.of(false));

        String token = service.login("admin", "wrong-password");

        assertNull(token);
        verify(jdbc).query(eq(FIND_ADMIN_SQL), any(RowMapper.class), eq("admin"));
    }

    @Test
    void isValidReturnsFalseForUnknownToken() {
        AuthService service = new AuthService(jdbc, hasher, "admin", "");

        assertEquals(false, service.isValid("unknown-token"));
    }
}
