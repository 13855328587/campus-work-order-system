package com.example.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("idempotency_record")
public class IdempotencyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String idempotencyKey;
    private Long userId;
    private Long orderId;
    private String requestHash;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
