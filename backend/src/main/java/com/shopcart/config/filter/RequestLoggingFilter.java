package com.shopcart.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Generate unique request ID for tracing
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.addHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        // Log request info
        log.info("[{}] {} {} {} | Session: {}", 
                requestId,
                method,
                uri,
                queryString != null ? "?" + queryString : "",
                request.getSession(false) != null ? request.getSession().getId() : "NO_SESSION");

        // Log request headers
        if (request.getContentType() != null) {
            log.debug("[{}] Content-Type: {}", requestId, request.getContentType());
        }

        try {
            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Calculate response time
            long duration = System.currentTimeMillis() - startTime;

            // Log response info
            int statusCode = response.getStatus();
            String statusLabel = statusCode >= 400 ? "ERROR" : "OK";
            
            log.info("[{}] {} {} ms | Status: {} ({})",
                    requestId,
                    statusLabel,
                    duration,
                    statusCode,
                    getStatusText(statusCode));

            // Log slow requests (> 1000ms)
            if (duration > 1000) {
                log.warn("[{}] SLOW REQUEST: {} {} took {}ms",
                        requestId,
                        method,
                        uri,
                        duration);
            }
        }
    }

    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "CREATED";
            case 204 -> "NO_CONTENT";
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> "UNPROCESSABLE_ENTITY";
            case 500 -> "INTERNAL_SERVER_ERROR";
            case 503 -> "SERVICE_UNAVAILABLE";
            default -> "UNKNOWN";
        };
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip logging for health check endpoints if any
        return path.startsWith("/actuator/health");
    }
}
