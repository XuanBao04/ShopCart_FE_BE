package com.shopcart.config;

import com.shopcart.entity.User;
import com.shopcart.entity.enums.UserRole;
import com.shopcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer - Creates default users on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        // Create default customer user
        if (!userRepository.existsByUsername("customer1")) {
            User customer = User.builder()
                    .username("customer1")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Customer One")
                    .email("customer1@shopcart.com")
                    .role(UserRole.CUSTOMER)
                    .build();
            userRepository.save(customer);
            log.info("Created default customer user: customer1");
        }

        // Create default admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .email("admin@shopcart.com")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: admin");
        }
    }
}
