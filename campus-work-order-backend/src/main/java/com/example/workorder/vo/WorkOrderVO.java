package com.example.workorder.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkOrderVO {
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
    private List<String> imageUrls;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
