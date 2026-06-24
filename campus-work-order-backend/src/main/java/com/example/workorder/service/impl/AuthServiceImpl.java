package com.example.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workorder.dto.LoginRequest;
import com.example.workorder.dto.RegisterRequest;
import com.example.workorder.entity.SysUser;
import com.example.workorder.enums.UserRole;
import com.example.workorder.exception.BusinessException;
import com.example.workorder.mapper.SysUserMapper;
import com.example.workorder.security.JwtUtil;
import com.example.workorder.security.AuthSessionService;
import com.example.workorder.service.AuthService;
import com.example.workorder.service.CaptchaService;
import com.example.workorder.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    private final AuthSessionService authSessionService;

    @Override
    public void register(RegisterRequest request) {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.STUDENT.name());
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        captchaService.validate(request.getCaptchaId(), request.getCaptchaCode());

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        authSessionService.create(token, user.getId());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole(),user.getPhone(),user.getAvatarUrl());
    }
}
