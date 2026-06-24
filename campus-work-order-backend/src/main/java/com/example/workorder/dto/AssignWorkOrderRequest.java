package com.example.workorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignWorkOrderRequest {
    @NotNull(message = "维修人员ID不能为空")
    private Long handlerId;
}
