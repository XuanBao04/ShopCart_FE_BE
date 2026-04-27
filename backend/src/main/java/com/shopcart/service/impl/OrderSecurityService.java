package com.shopcart.service.impl;

import com.shopcart.entity.User;
import com.shopcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service for order-level authorization checks.
 * Used in @PreAuthorize SpEL expressions.
 */
@Service("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final UserRepository userRepository;

    /**
     * Check if the authenticated user is the owner of the given userId.
     * userId in orders is stored as String(user.id), while authentication.name is the username.
     */
    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> String.valueOf(user.getId()).equals(userId))
                .orElse(false);
    }
}
