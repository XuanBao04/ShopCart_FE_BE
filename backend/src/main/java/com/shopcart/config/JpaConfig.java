package com.shopcart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.shopcart.repository")
@EnableJpaAuditing
public class JpaConfig {
    // JPA configuration
}
