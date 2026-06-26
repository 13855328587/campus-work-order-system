package com.example.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workorder.common.Result;
import com.example.workorder.dto.BatchUserRequest;
import com.example.workorder.dto.CreateUserRequest;
import com.example.workorder.dto.UpdateUserRequest;
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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class UserController {
    private final SysUserMapper sysUserMapper;
    private final WorkOrderMapper workOrderMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    public Result<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        String currentRole = SecurityUtils.getRole();
        String targetRole = request.getRole();

        checkCreatePermission(currentRole, targetRole);

        String username = request.getUsername().trim();
        Long exists = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(request.getRealName().trim());
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setRole(targetRole);
        user.setStatus(1);
        sysUserMapper.insert(user);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "roles", required = false) String roles,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {

        // 管理端用户分页查询，支持基础信息、角色、状态、时间范围筛选。
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        List<String> roleList = StringUtils.hasText(roles)
                ? Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList()
                : List.of();

        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();

        qw.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .eq(StringUtils.hasText(role), SysUser::getRole, role)
                .in(!StringUtils.hasText(role) && !roleList.isEmpty(), SysUser::getRole, roleList)
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

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable("id") Long id,
                                   @RequestBody @Valid UpdateUserRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        SysUser targetUser = sysUserMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        checkBasicInfoPermission(currentUserId, currentRole, targetUser);

        SysUser update = new SysUser();
        update.setId(id);
        update.setRealName(request.getRealName().trim());
        update.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        sysUserMapper.updateById(update);
        return Result.success();
    }

    /**
     * 管理员重置指定用户密码。
     * 普通管理员只能操作维修人员和学生；超级管理员可以操作管理员、维修人员和学生。
     */
    @PutMapping("/{id}/reset-password")
    @Transactional
    public Result<Void> resetPassword(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        if (currentUserId.equals(id)) {
            throw new BusinessException(400, "不能重置自己的密码");
        }

        SysUser targetUser = sysUserMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        checkBasicInfoPermission(currentUserId, currentRole, targetUser);

        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode("123456"));
        sysUserMapper.updateById(update);
        return Result.success();
    }

    @PutMapping("/batch/reset-password")
    @Transactional
    public Result<Void> batchResetPassword(@RequestBody @Valid BatchUserRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        List<Long> ids = distinctIds(request.getIds());
        if (ids.contains(currentUserId)) {
            throw new BusinessException(400, "不能重置自己的密码");
        }

        List<SysUser> users = findUsersByIds(ids);
        if (users.size() != ids.size()) {
            throw new BusinessException(404, "存在用户不存在或已删除");
        }

        // 批量操作前先统一校验权限，避免部分成功、部分失败造成数据不一致。
        users.forEach(user -> checkBasicInfoPermission(currentUserId, currentRole, user));

        String encodedPassword = passwordEncoder.encode("123456");
        users.forEach(user -> {
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setPassword(encodedPassword);
            sysUserMapper.updateById(update);
        });

        return Result.success();
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

        // 禁用维修人员时释放待处理/处理中工单，退回待处理，方便管理员重新分配；已完成历史保持不变。
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

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        if (currentUserId.equals(id)) {
            throw new BusinessException(400, "不能删除自己的账号");
        }

        SysUser targetUser = sysUserMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        checkStatusPermission(currentRole, targetUser.getRole());

        // 删除维修人员采用逻辑删除；删除前释放待处理和处理中的工单，避免工单继续挂在已删除员工名下。
        if (UserRole.WORKER.name().equals(targetUser.getRole())) {
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

        sysUserMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/batch/delete")
    @Transactional
    public Result<Void> batchDeleteUser(@RequestBody @Valid BatchUserRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        String currentRole = SecurityUtils.getRole();

        List<Long> ids = distinctIds(request.getIds());
        if (ids.contains(currentUserId)) {
            throw new BusinessException(400, "不能删除自己的账号");
        }

        List<SysUser> users = findUsersByIds(ids);
        if (users.size() != ids.size()) {
            throw new BusinessException(404, "存在用户不存在或已删除");
        }

        // 先检查所有用户权限，再执行工单释放和逻辑删除，保证事务一致性。
        users.forEach(user -> checkStatusPermission(currentRole, user.getRole()));

        List<Long> workerIds = users.stream()
                .filter(user -> UserRole.WORKER.name().equals(user.getRole()))
                .map(SysUser::getId)
                .toList();

        if (!workerIds.isEmpty()) {
            workOrderMapper.update(null,
                    new LambdaUpdateWrapper<WorkOrder>()
                            .in(WorkOrder::getHandlerId, workerIds)
                            .in(WorkOrder::getStatus,
                                    WorkOrderStatus.PENDING_PROCESS.name(),
                                    WorkOrderStatus.PROCESSING.name())
                            .set(WorkOrder::getHandlerId, null)
                            .set(WorkOrder::getStatus, WorkOrderStatus.PENDING_PROCESS.name())
            );
        }

        ids.forEach(sysUserMapper::deleteById);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    @Transactional
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

        // 维修人员改成非维修角色时，释放待处理/处理中工单；已完成等历史工单保持原记录。
        if (UserRole.WORKER.name().equals(targetUser.getRole())
                && !UserRole.WORKER.name().equals(role)) {
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

        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        sysUserMapper.updateById(user);
        return Result.success();
    }

    private void checkStatusPermission(String currentRole, String targetRole) {
        // 状态修改/删除共用权限：超级管理员不能操作超级管理员，管理员不能操作管理员或超级管理员。
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
        // 角色修改权限比基础资料更严格，禁止把任何人改成超级管理员。
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

    private void checkCreatePermission(String currentRole, String targetRole) {
        // 新增用户不允许创建超级管理员；超级管理员可新增管理员/维修人员/学生，管理员可新增维修人员/学生。
        if (UserRole.SUPER_ADMIN.name().equals(targetRole)) {
            throw new BusinessException(403, "不允许新增超级管理员");
        }

        if (UserRole.SUPER_ADMIN.name().equals(currentRole)) {
            if (UserRole.ADMIN.name().equals(targetRole)
                    || UserRole.WORKER.name().equals(targetRole)
                    || UserRole.STUDENT.name().equals(targetRole)) {
                return;
            }
            throw new BusinessException(400, "用户角色不正确");
        }

        if (UserRole.ADMIN.name().equals(currentRole)) {
            if (UserRole.WORKER.name().equals(targetRole)
                    || UserRole.STUDENT.name().equals(targetRole)) {
                return;
            }
            throw new BusinessException(403, "管理员只能新增维修人员和学生");
        }

        throw new BusinessException(403, "无权限新增用户");
    }

    private void checkBasicInfoPermission(Long currentUserId, String currentRole, SysUser targetUser) {
        // 基础资料允许用户修改自己；管理员只能修改权限范围内的普通用户。
        if (currentUserId.equals(targetUser.getId())) {
            return;
        }
        if (UserRole.SUPER_ADMIN.name().equals(currentRole)) {
            return;
        }
        if (UserRole.ADMIN.name().equals(currentRole)
                && (UserRole.WORKER.name().equals(targetUser.getRole())
                || UserRole.STUDENT.name().equals(targetUser.getRole()))) {
            return;
        }
        throw new BusinessException(403, "无权限修改该用户信息");
    }

    private List<Long> distinctIds(List<Long> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<SysUser> findUsersByIds(List<Long> ids) {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, ids)
        );
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
