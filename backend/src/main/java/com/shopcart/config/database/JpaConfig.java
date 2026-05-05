package com.shopcart.config.database;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EntityScan(basePackages = "com.shopcart")
@EnableJpaAuditing
public class JpaConfig {
    // JPA configuration - Repository scanning is auto-configured by Spring Boot
}
