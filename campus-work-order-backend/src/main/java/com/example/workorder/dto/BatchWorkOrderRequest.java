package com.example.workorder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchWorkOrderRequest {
    @NotEmpty(message = "请选择工单")
    private List<@NotNull(message = "工单ID不能为空") Long> ids;
}
