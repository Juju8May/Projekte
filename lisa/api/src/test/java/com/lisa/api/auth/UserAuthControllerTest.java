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

}
    