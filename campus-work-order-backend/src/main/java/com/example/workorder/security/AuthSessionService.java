package com.example.workorder.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private static final String KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate redisTemplate;

    @Value("${auth.session-expiration:24h}")
    private Duration expiration;

    public void create(String token, Long userId) {
        redisTemplate.opsForValue().set(key(token), userId.toString(), expiration);
    }

    public boolean isValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    public void delete(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法生成会话标识", e);
        }
    }
}
