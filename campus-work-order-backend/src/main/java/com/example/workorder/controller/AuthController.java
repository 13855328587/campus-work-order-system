package com.example.workorder.controller;

import com.example.workorder.common.Result;
import com.example.workorder.dto.LoginRequest;
import com.example.workorder.dto.RegisterRequest;
import com.example.workorder.security.SecurityUtils;
import com.example.workorder.security.AuthSessionService;
import com.example.workorder.service.AuthService;
import com.example.workorder.service.CaptchaService;
import com.example.workorder.vo.CaptchaVO;
import com.example.workorder.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CaptchaService captchaService;
    private final AuthSessionService authSessionService;

    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.success(captchaService.generate());
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authSessionService.delete(authorization.substring(7));
        }
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Object> me() {
        return Result.success(SecurityUtils.getLoginUser());
    }
}
