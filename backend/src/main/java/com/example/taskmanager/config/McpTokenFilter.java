package com.example.taskmanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class McpTokenFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-MCP-Token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/mcp")) {
            String required = System.getenv("MCP_TOKEN");
            // If MCP_TOKEN is set (non-empty), require header to match. If not set, allow access (development mode).
            if (required != null && !required.isEmpty()) {
                String got = request.getHeader(HEADER_NAME);
                if (got == null || !got.equals(required)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Missing or invalid X-MCP-Token\"}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
