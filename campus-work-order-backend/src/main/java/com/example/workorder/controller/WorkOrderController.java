package com.example.workorder.controller;

import com.example.workorder.common.Result;
import com.example.workorder.dto.AssignWorkOrderRequest;
import com.example.workorder.dto.BatchWorkOrderRequest;
import com.example.workorder.dto.CreateWorkOrderRequest;
import com.example.workorder.dto.FinishWorkOrderRequest;
import com.example.workorder.dto.RejectWorkOrderRequest;
import com.example.workorder.entity.PageResult;
import com.example.workorder.entity.WorkOrder;
import com.example.workorder.service.WorkOrderService;
import com.example.workorder.vo.WorkOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<PageResult<WorkOrder>> page(

            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,

            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(workOrderService.pageQuery(
                pageNum, pageSize,
                orderNo, title, location, category, priority, status,
                startTime, endTime
        ));
    }
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<WorkOrderVO> create(@RequestBody @Valid CreateWorkOrderRequest request,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return Result.success(workOrderService.create(request, idempotencyKey));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<WorkOrderVO>> myOrders(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(workOrderService.myOrders(
                pageNum, pageSize,
                orderNo, title, location, category, priority, status,
                startTime, endTime
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<List<WorkOrderVO>> listAll() {
        return Result.success(workOrderService.listAll());
    }

    @GetMapping("/{id}")
    public Result<WorkOrderVO> detail(@PathVariable("id") Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> approve(@PathVariable("id") Long id) {
        workOrderService.approve(id);
        return Result.success();
    }

    @PostMapping("/batch/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> batchApprove(@RequestBody @Valid BatchWorkOrderRequest request) {
        workOrderService.batchApprove(request.getIds());
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> reject(@PathVariable("id") Long id, @RequestBody @Valid RejectWorkOrderRequest request) {
        workOrderService.reject(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Void> assign(@PathVariable("id") Long id, @RequestBody @Valid AssignWorkOrderRequest request) {
        workOrderService.assign(id, request);
        return Result.success();
    }

    @GetMapping("/worker/tasks")
    @PreAuthorize("hasRole('WORKER')")
    public Result<PageResult<WorkOrderVO>> workerTasks(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(workOrderService.workerTasks(
                pageNum, pageSize,
                orderNo, title, location, category, priority, status,
                startTime, endTime
        ));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('WORKER')")
    public Result<Void> accept(@PathVariable("id") Long id) {
        workOrderService.accept(id);
        return Result.success();
    }

    @PostMapping("/batch/accept")
    @PreAuthorize("hasRole('WORKER')")
    public Result<Void> batchAccept(@RequestBody @Valid BatchWorkOrderRequest request) {
        workOrderService.batchAccept(request.getIds());
        return Result.success();
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('WORKER')")
    public Result<Void> finish(@PathVariable("id") Long id, @RequestBody @Valid FinishWorkOrderRequest request) {
        workOrderService.finish(id, request);
        return Result.success();
    }
    @GetMapping("/worker/history")
    @PreAuthorize("hasRole('WORKER')")
    public Result<List<WorkOrderVO>> workerHistory() {
        return Result.success(workOrderService.workerHistory());
    }
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        workOrderService.cancel(id);
        return Result.success();
    }

    @PostMapping("/batch/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> batchCancel(@RequestBody @Valid BatchWorkOrderRequest request) {
        workOrderService.batchCancel(request.getIds());
        return Result.success();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Map<String, Long>> statistics() {
        return Result.success(workOrderService.statistics());
    }
}
