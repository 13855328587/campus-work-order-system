package com.example.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.workorder.common.Result;
import com.example.workorder.entity.SysUser;
import com.example.workorder.mapper.SysUserMapper;
import com.example.workorder.security.SecurityUtils;
import com.example.workorder.entity.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final OssService ossService;
    private final SysUserMapper sysUserMapper;

    @PostMapping("/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {

        Long userId = SecurityUtils.getUserId(); // ⭐正确方式

        String url = ossService.upload(file);

        sysUserMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getAvatarUrl, url)
        );

        return Result.success(url);
    }

    @PostMapping("/work-order-images")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> uploadWorkOrderImage(@RequestParam("file") MultipartFile file) {
        return Result.success(ossService.uploadWorkOrderImage(file));
    }
}
