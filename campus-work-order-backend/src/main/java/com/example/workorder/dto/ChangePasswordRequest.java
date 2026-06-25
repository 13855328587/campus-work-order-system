package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "原密码不能为空")
    @Size(max = 30, message = "原密码不能超过30个字符")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 30, message = "新密码长度必须在8到30个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d_@#$%^&*!.?-]+$", message = "新密码必须包含字母和数字，可使用常见符号")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 8, max = 30, message = "确认密码长度必须在8到30个字符之间")
    private String confirmPassword;
}
