package com.lisa.api.auth;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class UserAuthControllerTest {
    @Mock
    private UserAuthService authService;    

    @Test
    void loginReturnsResponseEntityForValidLoginRequest() {
        UserAuthController controller = new UserAuthController(authService);
        UserAuthService.LoginResult loginResult = new UserAuthService.LoginResult(
            1L, "admin", "test-token", true, "test-conversation-id");
        when(authService.login("admin", "secret")).thenReturn(loginResult);

        ResponseEntity<?> response = controller.login(
                new UserAuthController.LoginRequest("  admin  ", "secret"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                Map.of("token", "test-token", "conversationId", "test-conversation-id", "expiresInSeconds", 8 * 60 * 60),
                response.getBody());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidLoginRequest() {
        UserAuthController controller = new UserAuthController(authService);
        when(authService.login("admin", "wrongpassword")).thenReturn(null);

        ResponseEntity<?> response = controller.login(
                new UserAuthController.LoginRequest("  admin  ", "wrongpassword"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void registerReturnsResponseEntityForValidRegisterRequest() {
        UserAuthController controller = new UserAuthController(authService);
        UserAuthService.LoginResult registerResult = new UserAuthService.LoginResult(
            1L, "newuser", "test-token", true, "test-conversation-id");
        when(authService.register("newuser", "password")).thenReturn(registerResult);

        ResponseEntity<?> response = controller.register(
                new UserAuthController.LoginRequest("  newuser  ", "password"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(
                Map.of("token", "test-token", "conversationId", "test-conversation-id", "expiresInSeconds", 8 * 60 * 60),
                response.getBody());
    }

    @Test
    void registerReturnsConflictForExistingUsername() {
        UserAuthController controller = new UserAuthController(authService);
        when(authService.register("existinguser", "password")).thenReturn(null);

        ResponseEntity<?> response = controller.register(
                new UserAuthController.LoginRequest("  existinguser  ", "password"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
    
}
