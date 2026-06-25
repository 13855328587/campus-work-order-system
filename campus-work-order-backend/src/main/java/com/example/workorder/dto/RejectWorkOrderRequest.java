package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectWorkOrderRequest {
    @NotBlank(message = "原因不能为空")
    @Size(max = 255, message = "原因不能超过255个字符")
    private String rejectReason;
}
