package com.shopcart.auth.service.impl;

import com.shopcart.constant.MessageConstant;

import com.shopcart.auth.dto.request.LoginRequest;
import com.shopcart.auth.dto.request.RegisterRequest;
import com.shopcart.auth.dto.response.AuthResponse;
import com.shopcart.user.entity.User;
import com.shopcart.common.enums.UserRole;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.InvalidInputException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.user.repository.UserRepository;
import com.shopcart.auth.service.IAuthService;
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
                        MessageConstant.Auth.USER_NOT_FOUND + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidInputException(MessageConstant.Auth.INVALID_PASSWORD);
        }

        return toAuthResponse(user, MessageConstant.Auth.LOGIN_SUCCESS);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessLogicException(
                    MessageConstant.Auth.USERNAME_EXISTS + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        return toAuthResponse(user, MessageConstant.Auth.REGISTER_SUCCESS);
    }

    @Override
    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageConstant.Auth.USER_NOT_FOUND + username));

        return toAuthResponse(user, MessageConstant.Auth.USER_INFO_RETRIEVED);
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
