package com.example.workorder.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String phone;
    private String avatarUrl;
}
