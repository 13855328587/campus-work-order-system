package com.example.workorder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchUserRequest {
    @NotEmpty(message = "请选择用户")
    private List<@NotNull(message = "用户ID不能为空") Long> ids;
}
