package com.lisa.api.auth;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserAuthControllerTest {
    @Mock
    private UserAuthService authService;

    @Test
    void loginReturnsTokenAndConversationForValidCredentials() {
        UserAuthService.LoginResult result = new UserAuthService.LoginResult(
                1L, "maya", "test-token", true, "conversation-1");
        when(authService.login("maya", "secret")).thenReturn(result);

        ResponseEntity<?> response = new UserAuthController(authService).login(
                new UserAuthController.LoginRequest("  maya  ", "secret"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("token", "test-token", "conversationId", "conversation-1",
                "expiresInSeconds", 8 * 60 * 60), response.getBody());
        verify(authService).login("maya", "secret");
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        when(authService.login("maya", "wrong-password")).thenReturn(null);

        ResponseEntity<?> response = new UserAuthController(authService).login(
                new UserAuthController.LoginRequest("maya", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid credentials"), response.getBody());
    }

    @Test
    void registerReturnsCreatedForNewUsername() {
        UserAuthService.LoginResult result = new UserAuthService.LoginResult(
                2L, "newuser", "new-token", true, "conversation-2");
        when(authService.register("newuser", "secret")).thenReturn(result);

        ResponseEntity<?> response = new UserAuthController(authService).register(
                new UserAuthController.LoginRequest("  newuser  ", "secret"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Map.of("token", "new-token", "conversationId", "conversation-2",
                "expiresInSeconds", 8 * 60 * 60), response.getBody());
        verify(authService).register("newuser", "secret");
    }

    @Test
    void registerReturnsConflictWhenUsernameAlreadyExists() {
        when(authService.register("existing", "secret")).thenReturn(null);

        ResponseEntity<?> response = new UserAuthController(authService).register(
                new UserAuthController.LoginRequest("existing", "secret"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Map.of("error", "Username already exists"), response.getBody());
    }

    @Test
    void sessionReturnsConversationForValidBearerToken() {
        UserAuthService.UserSession session = new UserAuthService.UserSession(
                "maya", "conversation-1", java.time.Instant.now().plusSeconds(60));
        when(authService.session("valid-token")).thenReturn(session);

        ResponseEntity<Map<String, Object>> response = new UserAuthController(authService)
                .session("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", true, "conversationId", "conversation-1"), response.getBody());
        verify(authService).session("valid-token");
    }

    @Test
    void sessionReturnsUnauthenticatedWhenTokenIsMissing() {
        when(authService.session("")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = new UserAuthController(authService).session(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", false), response.getBody());
        verify(authService).session("");
    }
}
