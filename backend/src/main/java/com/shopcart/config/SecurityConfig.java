package com.shopcart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security Configuration
 * - Session-based authentication
 * - CORS configuration for frontend
 * - Public and protected endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // CORS configuration
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Disable CSRF for REST API
                                .csrf(csrf -> csrf.disable())

                                // Session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints - no authentication required
                                                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                                                .requestMatchers("/api/auth/logout").permitAll()
                                                .requestMatchers("/api/products/**").permitAll()
                                                .requestMatchers("/api/inventory/**").permitAll()
                                                .requestMatchers("/api/cart/**").permitAll()
                                                .requestMatchers("/api/orders/**").authenticated()
                                                // .requestMatchers(HttpMethod.GET, "/api/cart/**").permitAll()
                                                // .requestMatchers(HttpMethod.POST, "/api/cart/**").permitAll()
                                                // .requestMatchers(HttpMethod.PATCH, "/api/cart/**").permitAll()
                                                // .requestMatchers(HttpMethod.PUT, "/api/cart/**").permitAll()
                                                // .requestMatchers(HttpMethod.DELETE, "/api/cart/**").permitAll()

                                                // H2 Console (development only)
                                                .requestMatchers("/h2-console/**").permitAll()

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())

                                // Return 401 for unauthenticated requests (instead of redirect/403)
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Vui lòng đăng nhập để tiếp tục.\"}");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Bạn không có quyền truy cập.\"}");
                                                }))

                                // Allow H2 Console frames
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "http://localhost:3000"));
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);
                return source;
        }
}
