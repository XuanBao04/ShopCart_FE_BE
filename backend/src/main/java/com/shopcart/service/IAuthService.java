package com.shopcart.service;

import com.shopcart.dto.request.LoginRequest;
import com.shopcart.dto.request.RegisterRequest;
import com.shopcart.dto.response.AuthResponse;

/**
 * Service interface for Authentication operations
 */
public interface IAuthService {
    
    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse getCurrentUser(String username);
}
