package com.shopcart.order.service.impl;

import com.shopcart.user.entity.User;
import com.shopcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final UserRepository userRepository;

    
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
