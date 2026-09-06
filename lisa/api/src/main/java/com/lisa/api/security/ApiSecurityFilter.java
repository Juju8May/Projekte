package com.lisa.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiSecurityFilter extends OncePerRequestFilter {
    private final String apiKey;
    private final int limitPerMinute;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public ApiSecurityFilter(
            @Value("${app.security.api-key}") String apiKey,
            @Value("${app.security.rate-limit-per-minute:60}") int limitPerMinute) {
        this.apiKey = apiKey;
        this.limitPerMinute = Math.max(1, limitPerMinute);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String suppliedKey = request.getHeader("X-Api-Key");
        if (apiKey.isBlank() || suppliedKey == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8), suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String clientKey = request.getRemoteAddr();
        Window window = windows.compute(clientKey, (key, current) -> {
            long now = Instant.now().getEpochSecond();
            if (current == null || now - current.startedAt >= 60) return new Window(now, 1);
            return new Window(current.startedAt, current.count + 1);
        });
        if (window.count > limitPerMinute) {
            response.setHeader(HttpHeaders.RETRY_AFTER, "60");
            reject(response, 429, "Too many requests");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("api-client", null, AuthorityUtils.NO_AUTHORITIES));
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private record Window(long startedAt, int count) { }
}
