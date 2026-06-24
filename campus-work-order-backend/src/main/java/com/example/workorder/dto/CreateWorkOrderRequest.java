package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateWorkOrderRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    private String description;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotBlank(message = "类型不能为空")
    private String category;

    @NotBlank(message = "优先级不能为空")
    private String priority;

    @Size(max = 5, message = "工单图片最多上传5张")
    private List<String> imageUrls;
}
