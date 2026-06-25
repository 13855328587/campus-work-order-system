package com.example.workorder.mq;

public record WorkOrderChangedEvent(
        Long orderId,
        Long operatorId,
        String operation,
        String beforeStatus,
        String afterStatus,
        String remark,
        long occurredAt
) {
}
