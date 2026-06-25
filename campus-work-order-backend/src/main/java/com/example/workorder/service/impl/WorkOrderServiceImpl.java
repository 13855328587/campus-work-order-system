package com.example.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.workorder.dto.AssignWorkOrderRequest;
import com.example.workorder.dto.CreateWorkOrderRequest;
import com.example.workorder.dto.FinishWorkOrderRequest;
import com.example.workorder.dto.RejectWorkOrderRequest;
import com.example.workorder.entity.*;
import com.example.workorder.enums.UserRole;
import com.example.workorder.enums.WorkOrderStatus;
import com.example.workorder.exception.BusinessException;
import com.example.workorder.mapper.IdempotencyRecordMapper;
import com.example.workorder.mapper.OperationLogMapper;
import com.example.workorder.mapper.SysUserMapper;
import com.example.workorder.mapper.WorkOrderMapper;
import com.example.workorder.mq.WorkOrderChangedEvent;
import com.example.workorder.security.SecurityUtils;
import com.example.workorder.service.WorkOrderService;
import com.example.workorder.vo.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {
    private final WorkOrderMapper workOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final IdempotencyRecordMapper idempotencyRecordMapper;
    private final OperationLogMapper operationLogMapper;
    private final OssService ossService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public WorkOrderVO create(CreateWorkOrderRequest request, String idempotencyKey) {
        Long userId = SecurityUtils.getUserId();

        // 同一用户重试同一个请求时返回首次创建结果，避免网络重试产生重复工单。
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BusinessException("缺少 Idempotency-Key 请求头");
        }

        IdempotencyRecord oldRecord = idempotencyRecordMapper.selectOne(
                new LambdaQueryWrapper<IdempotencyRecord>()
                        .eq(IdempotencyRecord::getIdempotencyKey, idempotencyKey)
                        .eq(IdempotencyRecord::getUserId, userId)
        );
        if (oldRecord != null && oldRecord.getOrderId() != null) {
            return detail(oldRecord.getOrderId());
        }

        // 新建工单默认进入待审核状态，由管理员审核通过后再进入待处理。
        WorkOrder order = new WorkOrder();
        order.setOrderNo(generateOrderNo());
        order.setCreatorId(userId);
        order.setTitle(request.getTitle());
        order.setDescription(request.getDescription());
        order.setLocation(request.getLocation());
        order.setCategory(request.getCategory());
        order.setPriority(request.getPriority());
        order.setImageUrls(serializeImageUrls(request.getImageUrls()));
        order.setStatus(WorkOrderStatus.PENDING_REVIEW.name());
        order.setVersion(0);
        workOrderMapper.insert(order);

        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setUserId(userId);
        record.setOrderId(order.getId());
        record.setRequestHash(String.valueOf(request.hashCode()));
        idempotencyRecordMapper.insert(record);

        saveLog(userId, order.getId(), "CREATE", null, order.getStatus(), "创建工单");
        return toVO(order);
    }

    @Override
    public PageResult<WorkOrderVO> myOrders(
            int pageNum,
            int pageSize,
            String orderNo,
            String title,
            String location,
            String category,
            String priority,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Long userId = SecurityUtils.getUserId();
        Page<WorkOrder> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getCreatorId, userId)
                .like(StringUtils.hasText(orderNo), WorkOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(title), WorkOrder::getTitle, title)
                .like(StringUtils.hasText(location), WorkOrder::getLocation, location)
                .eq(StringUtils.hasText(category), WorkOrder::getCategory, category)
                .eq(StringUtils.hasText(priority), WorkOrder::getPriority, priority)
                .eq(StringUtils.hasText(status), WorkOrder::getStatus, status)
                .ge(startTime != null, WorkOrder::getCreatedAt, startTime)
                .le(endTime != null, WorkOrder::getCreatedAt, endTime)
                .orderByDesc(WorkOrder::getCreatedAt)
                .orderByDesc(WorkOrder::getId);

        IPage<WorkOrder> result = workOrderMapper.selectPage(page, qw);
        List<WorkOrderVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, result.getTotal());
    }

    @Override
    public List<WorkOrderVO> listAll() {
        return workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                        .orderByDesc(WorkOrder::getCreatedAt))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorkOrderVO detail(Long id) {
        WorkOrder order = getOrder(id);
        String role = SecurityUtils.getRole();
        Long userId = SecurityUtils.getUserId();

        // 数据归属校验必须放在后端，不能只依赖前端隐藏按钮或路由。
        if (UserRole.STUDENT.name().equals(role) && !order.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只能查看自己的工单");
        }
        if (UserRole.WORKER.name().equals(role) && order.getHandlerId() != null && !order.getHandlerId().equals(userId)) {
            throw new BusinessException(403, "只能查看分配给自己的工单");
        }

        return toVO(order);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        WorkOrder order = getOrder(id);
        // 审核只允许从待审核进入待处理，禁止跨状态调用接口。
        checkStatus(order, WorkOrderStatus.PENDING_REVIEW);
        String before = order.getStatus();
        order.setStatus(WorkOrderStatus.PENDING_PROCESS.name());
        updateOrderOrThrow(order);
        saveLog(SecurityUtils.getUserId(), id, "APPROVE", before, order.getStatus(), "审核通过");
    }

    @Override
    @Transactional
    public void batchApprove(List<Long> ids) {
        List<Long> distinctIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            throw new BusinessException("请选择工单");
        }

        // 批量通过复用单个通过逻辑，确保状态校验、日志记录规则保持一致。
        for (Long id : distinctIds) {
            approve(id);
        }
    }

    @Override
    @Transactional
    public void reject(Long id, RejectWorkOrderRequest request) {
        WorkOrder order = getOrder(id);
        checkStatus(order, WorkOrderStatus.PENDING_REVIEW);
        String before = order.getStatus();
        order.setStatus(WorkOrderStatus.REJECTED.name());
        order.setRejectReason(request.getRejectReason());
        updateOrderOrThrow(order);
        saveLog(SecurityUtils.getUserId(), id, "REJECT", before, order.getStatus(), request.getRejectReason());
    }

    @Override
    @Transactional
    public void assign(Long id, AssignWorkOrderRequest request) {
        WorkOrder order = getOrder(id);
        checkStatus(order, WorkOrderStatus.PENDING_PROCESS);

        // 分配时再次校验角色和启用状态，防止绕过下拉列表提交禁用员工 ID。
        SysUser worker = sysUserMapper.selectById(request.getHandlerId());
        if (worker == null || !UserRole.WORKER.name().equals(worker.getRole())
                || worker.getStatus() == null || worker.getStatus() != 1) {
            throw new BusinessException("维修人员不存在或已被禁用");
        }

        order.setHandlerId(request.getHandlerId());
        updateOrderOrThrow(order);
        saveLog(SecurityUtils.getUserId(), id, "ASSIGN", order.getStatus(), order.getStatus(), "分配维修人员：" + request.getHandlerId());
    }

    @Override
    @Transactional
    public void accept(Long id) {
        WorkOrder order = getOrder(id);
        checkStatus(order, WorkOrderStatus.PENDING_PROCESS);

        Long userId = SecurityUtils.getUserId();
        SysUser worker = sysUserMapper.selectById(userId);
        if (worker == null || worker.getStatus() == null || worker.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，不能接单");
        }
        if (order.getHandlerId() != null && !order.getHandlerId().equals(userId)) {
            throw new BusinessException(403, "该工单未分配给当前维修人员");
        }

        String before = order.getStatus();
        order.setHandlerId(userId);
        order.setStatus(WorkOrderStatus.PROCESSING.name());

        // WorkOrder.version 参与 UPDATE 条件；多人同时接单时只有一个请求能更新成功。
        int rows = workOrderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(409, "工单已被其他人处理，请刷新后重试");
        }
        saveLog(userId, id, "ACCEPT", before, order.getStatus(), "维修人员接单");
    }

    @Override
    @Transactional
    public void batchAccept(List<Long> ids) {
        List<Long> distinctIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            throw new BusinessException("请选择工单");
        }

        // 批量接单仍逐个走 accept，保留每个工单的乐观锁并发保护。
        for (Long id : distinctIds) {
            accept(id);
        }
    }

    @Override
    @Transactional
    public void finish(Long id, FinishWorkOrderRequest request) {
        WorkOrder order = getOrder(id);
        checkStatus(order, WorkOrderStatus.PROCESSING);

        // 维修人员只能完成自己正在处理的工单，防止越权提交处理结果。
        Long userId = SecurityUtils.getUserId();
        if (!userId.equals(order.getHandlerId())) {
            throw new BusinessException(403, "只能完成自己处理中的工单");
        }

        String before = order.getStatus();
        order.setStatus(WorkOrderStatus.COMPLETED.name());
        order.setFinishResult(request.getFinishResult());
        updateOrderOrThrow(order);
        saveLog(userId, id, "FINISH", before, order.getStatus(), request.getFinishResult());
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        WorkOrder order = getOrder(id);
        Long userId = SecurityUtils.getUserId();

        // 学生只能取消自己提交的工单。
        if (!order.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只能取消自己的工单");
        }

        if (!WorkOrderStatus.PENDING_REVIEW.name().equals(order.getStatus())
                && !WorkOrderStatus.PENDING_PROCESS.name().equals(order.getStatus())) {
            throw new BusinessException("当前状态不能取消");
        }

        String before = order.getStatus();
        order.setStatus(WorkOrderStatus.CANCELLED.name());
        updateOrderOrThrow(order);
        saveLog(userId, id, "CANCEL", before, order.getStatus(), "学生取消工单");
    }

    @Override
    @Transactional
    public void batchCancel(List<Long> ids) {
        List<Long> distinctIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            throw new BusinessException("请选择工单");
        }

        // 批量取消复用单个取消逻辑，统一校验归属和状态。
        for (Long id : distinctIds) {
            cancel(id);
        }
    }

    @Override
    public Map<String, Long> statistics() {
        List<WorkOrder> orders = workOrderMapper.selectList(null);
        return orders.stream().collect(Collectors.groupingBy(WorkOrder::getStatus, Collectors.counting()));
    }

    @Override
    public PageResult<WorkOrderVO> workerTasks(
            int pageNum,
            int pageSize,
            String orderNo,
            String title,
            String location,
            String category,
            String priority,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Long userId = SecurityUtils.getUserId();
        Page<WorkOrder> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<WorkOrder>()
                .and(wrapper -> wrapper
                        .eq(WorkOrder::getHandlerId, userId)
                        .or()
                        .isNull(WorkOrder::getHandlerId)
                )
                .in(WorkOrder::getStatus,
                        WorkOrderStatus.PENDING_PROCESS.name(),
                        WorkOrderStatus.PROCESSING.name())
                .like(StringUtils.hasText(orderNo), WorkOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(title), WorkOrder::getTitle, title)
                .like(StringUtils.hasText(location), WorkOrder::getLocation, location)
                .eq(StringUtils.hasText(category), WorkOrder::getCategory, category)
                .eq(StringUtils.hasText(priority), WorkOrder::getPriority, priority)
                .eq(StringUtils.hasText(status), WorkOrder::getStatus, status)
                .ge(startTime != null, WorkOrder::getCreatedAt, startTime)
                .le(endTime != null, WorkOrder::getCreatedAt, endTime)
                .orderByDesc(WorkOrder::getCreatedAt);

        IPage<WorkOrder> result = workOrderMapper.selectPage(page, qw);
        List<WorkOrderVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, result.getTotal());
    }

    @Override
    public List<WorkOrderVO> workerHistory() {
        Long userId = SecurityUtils.getUserId();

        return workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                        .eq(WorkOrder::getHandlerId, userId)
                        .eq(WorkOrder::getStatus, WorkOrderStatus.COMPLETED.name())
                        .orderByDesc(WorkOrder::getUpdatedAt))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<WorkOrder> pageQuery(
            int pageNum,
            int pageSize,
            String orderNo,
            String title,
            String location,
            String category,
            String priority,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Page<WorkOrder> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<>();

        qw.like(StringUtils.hasText(orderNo), WorkOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(title), WorkOrder::getTitle, title)
                .like(StringUtils.hasText(location), WorkOrder::getLocation, location)
                .eq(StringUtils.hasText(category), WorkOrder::getCategory, category)
                .eq(StringUtils.hasText(priority), WorkOrder::getPriority, priority)
                .eq(StringUtils.hasText(status), WorkOrder::getStatus, status)
                .ge(startTime != null, WorkOrder::getCreatedAt, startTime)
                .le(endTime != null, WorkOrder::getCreatedAt, endTime)
                .orderByDesc(WorkOrder::getCreatedAt)
                .orderByDesc(WorkOrder::getId);

        IPage<WorkOrder> result = workOrderMapper.selectPage(page, qw);

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    private WorkOrder getOrder(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "工单不存在");
        }
        return order;
    }

    private void checkStatus(WorkOrder order, WorkOrderStatus required) {
        if (!required.name().equals(order.getStatus())) {
            throw new BusinessException(409, "当前工单状态不允许该操作");
        }
    }

    private void updateOrderOrThrow(WorkOrder order) {
        // 统一处理乐观锁冲突，避免更新失败后仍然写入成功操作日志。
        if (workOrderMapper.updateById(order) == 0) {
            throw new BusinessException(409, "工单已被其他人更新，请刷新后重试");
        }
    }

    private WorkOrderVO toVO(WorkOrder order) {
        WorkOrderVO vo = new WorkOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setImageUrls(deserializeImageUrls(order.getImageUrls()));
        return vo;
    }

    private String serializeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        // 只接受本系统工单目录中的 OSS 地址，避免保存任意外链造成跟踪或内容风险。
        if (imageUrls.size() > 5 || imageUrls.stream().anyMatch(url -> !ossService.isWorkOrderImageUrl(url))) {
            throw new BusinessException("工单图片地址不合法，请重新上传");
        }
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            throw new BusinessException("工单图片保存失败");
        }
    }

    private List<String> deserializeImageUrls(String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imageUrls, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long millis = System.currentTimeMillis() % 1000000;
        return "WO" + date + millis;
    }

    private void saveLog(Long userId, Long orderId, String operation, String before, String after, String remark) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOrderId(orderId);
        log.setOperation(operation);
        log.setBeforeStatus(before);
        log.setAfterStatus(after);
        log.setRemark(remark);
        operationLogMapper.insert(log);

        applicationEventPublisher.publishEvent(new WorkOrderChangedEvent(
                orderId,
                userId,
                operation,
                before,
                after,
                remark,
                System.currentTimeMillis()
        ));
    }
}
