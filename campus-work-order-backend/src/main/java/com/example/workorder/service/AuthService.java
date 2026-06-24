package com.example.workorder.service;

import com.example.workorder.dto.LoginRequest;
import com.example.workorder.dto.RegisterRequest;
import com.example.workorder.vo.LoginVO;

public interface AuthService {
    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);
}
