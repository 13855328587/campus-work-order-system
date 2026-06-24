package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectWorkOrderRequest {
    @NotBlank(message = "驳回原因不能为空")
    private String rejectReason;
}
