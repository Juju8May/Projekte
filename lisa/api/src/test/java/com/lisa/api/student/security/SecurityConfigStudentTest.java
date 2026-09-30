package com.lisa.api.student.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.lisa.api.security.ApiSecurityFilter;
import com.lisa.api.security.SecurityConfig;

class SecurityConfigStudentTest {
    @Test
    void corsConfigurationUsesTrimmedAllowedOriginsAndExpectedHeaders() {
        SecurityConfig config = new SecurityConfig(
                mock(ApiSecurityFilter.class), " http://localhost:3000, , https://example.com ");

        CorsConfigurationSource source = ReflectionTestUtils.invokeMethod(config, "corsConfigurationSource");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/conversations");
        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:3000", "https://example.com"), cors.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "OPTIONS"), cors.getAllowedMethods());
        assertEquals(List.of("Content-Type", "X-Api-Key"), cors.getAllowedHeaders());
        assertEquals(List.of("Retry-After"), cors.getExposedHeaders());
        assertFalse(cors.getAllowCredentials());
    }
}
