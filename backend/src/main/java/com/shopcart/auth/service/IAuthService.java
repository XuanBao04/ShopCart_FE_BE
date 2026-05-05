package com.shopcart.auth.service;

import com.shopcart.auth.dto.request.LoginRequest;
import com.shopcart.auth.dto.request.RegisterRequest;
import com.shopcart.auth.dto.response.AuthResponse;

/**
 * Service interface for Authentication operations
 */
public interface IAuthService {
    
    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse getCurrentUser(String username);
}
