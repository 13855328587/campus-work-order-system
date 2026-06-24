package com.example.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long creatorId;
    private Long handlerId;
    private String title;
    private String description;
    private String location;
    private String category;
    private String priority;
    private String status;
    private String rejectReason;
    private String finishResult;
    /** JSON array containing the OSS URLs attached when the order was created. */
    private String imageUrls;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
