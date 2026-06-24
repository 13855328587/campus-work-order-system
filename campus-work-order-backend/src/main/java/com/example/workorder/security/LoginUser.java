package com.example.workorder.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String role;
}
