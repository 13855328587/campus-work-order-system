package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2到20个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z·]{2,20}$", message = "真实姓名只能包含中文、字母或间隔点")
    private String realName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "角色不能为空")
    private String role;
}
