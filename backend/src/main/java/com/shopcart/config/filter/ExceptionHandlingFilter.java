package com.shopcart.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.InvalidInputException;
import com.shopcart.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public ExceptionHandlingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ResourceNotFoundException ex) {
            handleException(request, response, HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
        } catch (InvalidInputException ex) {
            handleException(request, response, HttpStatus.BAD_REQUEST, "Invalid Input", ex.getMessage());
        } catch (BusinessLogicException ex) {
            handleException(request, response, HttpStatus.UNPROCESSABLE_ENTITY, "Business Logic Error", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            handleException(request, response, HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected exception in filter chain", ex);
            handleException(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", 
                    "An unexpected error occurred. Please try again later.");
        }
    }

   
    private void handleException(HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpStatus status,
                                 String error,
                                 String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", request != null ? request.getRequestURI() : "");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        log.warn("Exception handled in filter: {} - {}", error, message);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip exception handling for static resources
        return path.startsWith("/static/") || 
               path.startsWith("/public/") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg");
    }
}
