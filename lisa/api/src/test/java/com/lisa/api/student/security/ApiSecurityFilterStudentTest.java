package com.lisa.api.student.security;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import com.lisa.api.security.ApiSecurityFilter;

@ExtendWith(MockitoExtension.class)
class ApiSecurityFilterStudentTest {
    @Mock
    private FilterChain chain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void nonApiRequestBypassesApiKeyCheck() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsApiRequestWithoutValidApiKey() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/conversations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"error\":\"Unauthorized\"}", response.getContentAsString());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void acceptsApiRequestWithValidApiKey() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/conversations");
        request.addHeader("X-Api-Key", "test-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("api-client", authentication.getName());
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void optionsRequestBypassesApiKeyCheck() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/conversations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsApiRequestWhenConfiguredApiKeyIsBlank() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter(" ", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/conversations");
        request.addHeader("X-Api-Key", "test-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("{\"error\":\"Unauthorized\"}", response.getContentAsString());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void rateLimitsRequestsFromTheSameClient() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 1);
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/conversations");
        firstRequest.setRemoteAddr("192.0.2.10");
        firstRequest.addHeader("X-Api-Key", "test-key");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, chain);
        SecurityContextHolder.clearContext();

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/conversations");
        secondRequest.setRemoteAddr("192.0.2.10");
        secondRequest.addHeader("X-Api-Key", "test-key");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(secondRequest, secondResponse, chain);

        assertEquals(200, firstResponse.getStatus());
        assertEquals(429, secondResponse.getStatus());
        assertEquals("60", secondResponse.getHeader("Retry-After"));
        assertEquals("{\"error\":\"Too many requests\"}", secondResponse.getContentAsString());
        verify(chain, times(1)).doFilter(firstRequest, firstResponse);
        verify(chain, never()).doFilter(secondRequest, secondResponse);
    }
}
