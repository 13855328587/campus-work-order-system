package com.example.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workorder.common.Result;
import com.example.workorder.dto.ChangePasswordRequest;
import com.example.workorder.dto.UpdateProfileRequest;
import com.example.workorder.entity.SysUser;
import com.example.workorder.exception.BusinessException;
import com.example.workorder.mapper.SysUserMapper;
import com.example.workorder.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 修改当前登录用户的基础资料。
     * 这里只允许用户修改自己的用户名、真实姓名和手机号，不允许修改角色、状态等敏感字段。
     */
    @PutMapping
    @Transactional
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        Long userId = SecurityUtils.getUserId();
        String username = request.getUsername().trim();

        // 用户名具有唯一性；修改时需要排除当前登录用户自身。
        Long sameUsernameCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .ne(SysUser::getId, userId)
        );
        if (sameUsernameCount != null && sameUsernameCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setUsername(username);
        update.setRealName(request.getRealName().trim());
        update.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        sysUserMapper.updateById(update);
        return Result.success();
    }

    /**
     * 当前登录用户修改自己的密码。
     * 需要先校验原密码，再校验两次新密码一致，避免未授权修改。
     */
    @PutMapping("/password")
    @Transactional
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        Long userId = SecurityUtils.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(update);
        return Result.success();
    }
}
