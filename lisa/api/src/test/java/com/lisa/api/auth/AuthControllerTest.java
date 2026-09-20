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
public class AuthControllerTest {
    @Mock
    private AuthService authService;

    private AuthController authController;

    @Test
    void loginReturnsTokenForValidCredentials() {
        when(authService.login("admin", "secret")).thenReturn("test-token");

        ResponseEntity<?> response = authController.login(
                new AuthController.LoginRequest("  admin  ", "secret"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                Map.of("token", "test-token", "expiresInSeconds", 8 * 60 * 60),
                response.getBody());
        verify(authService).login("admin", "secret");
    }

    @Test
    void loginNotAllowedShouldFail() {
        when(authService.login("false", "loginPassword")).thenReturn(null);

        ResponseEntity<?> response = authController.login(
                new AuthController.LoginRequest("false", "loginPassword"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(
                Map.of("error", "Invalid credentials"),
                response.getBody());
        verify(authService).login("false", "loginPassword");
    }

    @Test
    void sessionReturnsAuthenticatedTrueForValidTokenWithHeader() {
        when(authService.isValid("validToken")).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = authController.session(
                "Bearer validToken"
                );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", true), response.getBody());
    }

    @Test
    void sessionReturnsAuthenticatedFalseForValidTokenWithoutHeader() {

        ResponseEntity<Map<String, Boolean>> response = authController.session(
                "validToken"
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("not authenticated", false), response.getBody());
    }
    
    @Test
    void sessionReturnsAuthenticatedFalseForInvalidToken() {
        when(authService.isValid("invalidToken")).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = authController.session(
                "Bearer invalidToken"
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("authenticated", false), response.getBody());
    }

}