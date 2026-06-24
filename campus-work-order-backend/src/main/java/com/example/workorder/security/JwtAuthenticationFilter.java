package com.example.workorder.security;

import com.example.workorder.entity.SysUser;
import com.example.workorder.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final AuthSessionService authSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                if (!authSessionService.isValid(token)) {
                    throw new IllegalStateException("登录会话不存在或已过期");
                }
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.valueOf(claims.get("userId").toString());
                SysUser user = sysUserMapper.selectById(userId);
                if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                    throw new IllegalStateException("用户不存在或已被禁用");
                }
                String username = user.getUsername();
                String role = user.getRole();

                LoginUser loginUser = new LoginUser(userId, username, role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginUser,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
