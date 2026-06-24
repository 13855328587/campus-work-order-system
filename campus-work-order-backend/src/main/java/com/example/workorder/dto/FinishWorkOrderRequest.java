package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FinishWorkOrderRequest {
    @NotBlank(message = "处理结果不能为空")
    private String finishResult;
}
