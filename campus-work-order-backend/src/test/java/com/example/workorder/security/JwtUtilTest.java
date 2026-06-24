package com.example.workorder.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "campus-work-order-test-secret-key-at-least-32-bytes"
        );
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        String token = jwtUtil.generateToken(3L, "student", "STUDENT");
        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(token);
        assertEquals("student", claims.getSubject());
        assertEquals(3L, ((Number) claims.get("userId")).longValue());
        assertEquals("STUDENT", claims.get("role", String.class));
    }
}
