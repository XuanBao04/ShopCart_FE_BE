package com.shopcart.service.impl;

import com.shopcart.dto.request.LoginRequest;
import com.shopcart.dto.request.RegisterRequest;
import com.shopcart.dto.response.AuthResponse;
import com.shopcart.entity.User;
import com.shopcart.entity.enums.UserRole;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.InvalidInputException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.repository.UserRepository;
import com.shopcart.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Authentication operations
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidInputException("Invalid password");
        }

        return toAuthResponse(user, "Login successful");
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessLogicException(
                    "Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        return toAuthResponse(user, "Registration successful");
    }

    @Override
    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));

        return toAuthResponse(user, "User info retrieved");
    }

    /**
     * Convert User entity to AuthResponse DTO
     */
    private AuthResponse toAuthResponse(User user, String message) {
        return AuthResponse.builder()
                .userId(user.getId())
                
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message(message)
                .build();
    }
}
