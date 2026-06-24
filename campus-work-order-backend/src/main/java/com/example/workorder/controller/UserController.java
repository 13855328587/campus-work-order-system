package com.example.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workorder.common.Result;
import com.example.workorder.entity.PageResult;
import com.example.workorder.entity.SysUser;
import com.example.workorder.entity.WorkOrder;
import com.example.workorder.enums.UserRole;
import com.example.workorder.enums.WorkOrderStatus;
import com.example.workorder.exception.BusinessException;
import com.example.workorder.mapper.SysUserMapper;
import com.example.workorder.mapper.WorkOrderMapper;
import com.example.workorder.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class UserController {
    private final SysUserMapper sysUserMapper;
    private final WorkOrderMapper workOrderMapper;


    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Page<SysUser> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();

        qw.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .eq(StringUtils.hasText(role), SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .ge(startTime != null, SysUser::getCreatedAt, startTime)
                .le(endTime != null, SysUser::getCreatedAt, endTime)
                .orderByAsc(SysUser::getId);

        IPage<SysUser> result = sysUserMapper.selectPage(page, qw);

        result.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(
                new PageResult<>(result.getRecords(), result.getTotal())
        );
    }
    @GetMapping
    public Result<List<SysUser>> list() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreatedAt));
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("用户状态只能是0或1");
        }
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        if (currentUserId.equals(id)) {
            throw new BusinessException(400, "不能修改自己的账号状态");
        }

        SysUser targetUser = sysUserMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        checkStatusPermission(currentRole, targetUser.getRole());

        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        sysUserMapper.updateById(user);

        if (status == 0 && UserRole.WORKER.name().equals(targetUser.getRole())) {
            workOrderMapper.update(null,
                    new LambdaUpdateWrapper<WorkOrder>()
                            .eq(WorkOrder::getHandlerId, id)
                            .in(WorkOrder::getStatus,
                                    WorkOrderStatus.PENDING_PROCESS.name(),
                                    WorkOrderStatus.PROCESSING.name())
                            .set(WorkOrder::getHandlerId, null)
                            .set(WorkOrder::getStatus, WorkOrderStatus.PENDING_PROCESS.name())
            );
        }
        return Result.success();
    }

    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable("id") Long id,
                                   @RequestParam("role") String role) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        if (currentUserId.equals(id)) {
            throw new BusinessException(400, "不能修改自己的角色");
        }

        SysUser targetUser = sysUserMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        checkRolePermission(currentRole, targetUser.getRole(), role);

        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        sysUserMapper.updateById(user);
        return Result.success();
    }

    private void checkStatusPermission(String currentRole, String targetRole) {
        if (UserRole.SUPER_ADMIN.name().equals(currentRole)) {
            if (UserRole.SUPER_ADMIN.name().equals(targetRole)) {
                throw new BusinessException(403, "超级管理员之间不允许互相修改状态");
            }
            return;
        }

        if (UserRole.ADMIN.name().equals(currentRole)) {
            if (UserRole.ADMIN.name().equals(targetRole) || UserRole.SUPER_ADMIN.name().equals(targetRole)) {
                throw new BusinessException(403, "管理员不能修改管理员或超级管理员状态");
            }
            return;
        }

        throw new BusinessException(403, "无权限修改用户状态");
    }

    private void checkRolePermission(String currentRole, String targetOldRole, String targetNewRole) {
        if (UserRole.SUPER_ADMIN.name().equals(currentRole)) {
            if (UserRole.SUPER_ADMIN.name().equals(targetOldRole) || UserRole.SUPER_ADMIN.name().equals(targetNewRole)) {
                throw new BusinessException(403, "不能修改超级管理员角色");
            }
            return;
        }

        if (UserRole.ADMIN.name().equals(currentRole)) {
            boolean oldRoleAllowed = UserRole.WORKER.name().equals(targetOldRole)
                    || UserRole.STUDENT.name().equals(targetOldRole);

            boolean newRoleAllowed = UserRole.WORKER.name().equals(targetNewRole)
                    || UserRole.STUDENT.name().equals(targetNewRole);

            if (!oldRoleAllowed || !newRoleAllowed) {
                throw new BusinessException(403, "管理员只能修改维修人员和学生角色");
            }
            return;
        }

        throw new BusinessException(403, "无权限修改用户角色");
    }
    @GetMapping("/workers")
    public Result<List<SysUser>> getWorkers() {
        return Result.success(
                sysUserMapper.selectList(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getRole, "WORKER")
                                .eq(SysUser::getStatus, 1)
                                .orderByAsc(SysUser::getId)
                )
        );
    }
}
