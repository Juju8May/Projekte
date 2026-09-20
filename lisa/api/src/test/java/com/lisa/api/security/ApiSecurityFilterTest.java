package com.lisa.api.security;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class ApiSecurityFilterTest {
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
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void optionsRequestBypassesApiKeyCheck() throws ServletException, IOException {
        ApiSecurityFilter filter = new ApiSecurityFilter("test-key", 60);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/conversations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}