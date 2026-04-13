package com.breaze.genesis.service;

import com.breaze.genesis.dto.request.LoginRequest;
import com.breaze.genesis.dto.request.RegisterRequest;
import com.breaze.genesis.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void register(RegisterRequest request);
}