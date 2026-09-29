package com.lisa.api.auth;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsTokenForValidCredentials() {
        when(authService.login("admin", "secret")).thenReturn("test-token");

        ResponseEntity<?> response = authController.login(
                new AuthController.LoginRequest("  admin  ", "secret"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("token", "test-token", "expiresInSeconds", 8 * 60 * 60), response.getBody());
        verify(authService).login("admin", "secret");
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        when(authService.login("admin", "wrong-password")).thenReturn(null);

        ResponseEntity<?> response = authController.login(
                new AuthController.LoginRequest("admin", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid credentials"), response.getBody());
        verify(authService).login("admin", "wrong-password");
    }

    @Test
    void sessionReturnsAuthenticatedTrueForValidBearerToken() {
        when(authService.isValid("valid-token")).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = authController.session("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", true), response.getBody());
        verify(authService).isValid("valid-token");
    }

    @Test
    void sessionRejectsMissingBearerToken() {
        ResponseEntity<Map<String, Boolean>> response = authController.session("valid-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("not authenticated", false), response.getBody());
    }

    @Test
    void sessionReturnsFalseForInvalidBearerToken() {
        when(authService.isValid("invalid-token")).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = authController.session("Bearer invalid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", false), response.getBody());
    }
}
