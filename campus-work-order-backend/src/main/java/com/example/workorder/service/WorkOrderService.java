package com.example.workorder.service;

import com.example.workorder.dto.AssignWorkOrderRequest;
import com.example.workorder.dto.CreateWorkOrderRequest;
import com.example.workorder.dto.FinishWorkOrderRequest;
import com.example.workorder.dto.RejectWorkOrderRequest;
import com.example.workorder.entity.PageResult;
import com.example.workorder.entity.WorkOrder;
import com.example.workorder.vo.WorkOrderVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface WorkOrderService {
    WorkOrderVO create(CreateWorkOrderRequest request, String idempotencyKey);
    PageResult<WorkOrderVO> myOrders(int pageNum, int pageSize, String orderNo, String title,
                                     String location, String category, String priority, String status,
                                     LocalDateTime startTime, LocalDateTime endTime);

    List<WorkOrderVO> listAll();

    WorkOrderVO detail(Long id);

    void approve(Long id);

    void batchApprove(List<Long> ids);

    void reject(Long id, RejectWorkOrderRequest request);

    void assign(Long id, AssignWorkOrderRequest request);

    void accept(Long id);

    void batchAccept(List<Long> ids);

    void finish(Long id, FinishWorkOrderRequest request);

    void cancel(Long id);

    void batchCancel(List<Long> ids);

    Map<String, Long> statistics();

    PageResult<WorkOrderVO> workerTasks(int pageNum, int pageSize, String orderNo, String title,
                                        String location, String category, String priority, String status,
                                        LocalDateTime startTime, LocalDateTime endTime);

    List<WorkOrderVO> workerHistory();

    PageResult<WorkOrder> pageQuery(int pageNum, int pageSize, String orderNo, String title,
                                    String location, String category, String priority, String status,
                                    LocalDateTime startTime, LocalDateTime endTime);
}
