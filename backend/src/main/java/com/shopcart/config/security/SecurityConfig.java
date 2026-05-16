package com.shopcart.config.security;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

        @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
        private String corsAllowedOrigins;

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

                                // Enable CSRF with CookieCsrfTokenRepository for REST APIs
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                                                .ignoringRequestMatchers("/h2-console/**", "/api/auth/**", "/api/csrf-token"))

                                // Session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Allow all OPTIONS requests for CORS preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                                                // Public endpoints - no authentication required
                                                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                                                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                                                .requestMatchers("/api/auth/logout").permitAll()
                                                .requestMatchers("/api/csrf-token").permitAll()
                                                .requestMatchers("/api/products/**").permitAll()
                                                .requestMatchers("/api/inventory/**").permitAll()
                                                .requestMatchers("/api/cart/**").authenticated()
                                                .requestMatchers("/api/orders/**").authenticated()
                                            
                                                .requestMatchers("/h2-console/**").permitAll()

                                     
                                                .anyRequest().authenticated())

                                
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
                                                .frameOptions(frame -> frame.sameOrigin()))
                                
                                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

                return http.build();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
                protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null && csrfToken.getHeaderName() != null) {
                String token = csrfToken.getToken();
                response.setHeader(csrfToken.getHeaderName(), token);
            }
            filterChain.doFilter(request, response);
        }
    }

    private static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return StringUtils.hasText(headerValue)
                    ? super.resolveCsrfTokenValue(request, csrfToken)
                    : this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                // Parse and trim allowed origins
                List<String> allowedOrigins = new ArrayList<>();
                for (String origin : corsAllowedOrigins.split(",")) {
                        String trimmedOrigin = origin.trim();
                        if (!trimmedOrigin.isEmpty()) {
                                allowedOrigins.add(trimmedOrigin);
                        }
                }
                
                logger.info("CORS allowed origins: {}", allowedOrigins);
                
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigins);
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);
                return source;
        }
}
