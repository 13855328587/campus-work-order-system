package com.example.workorder.service.impl;

import com.example.workorder.exception.BusinessException;
import com.example.workorder.service.CaptchaService;
import com.example.workorder.vo.CaptchaVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final String KEY_PREFIX = "auth:captcha:";
    private static final char[] CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final DefaultRedisScript<String> GET_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); " +
                    "if value then redis.call('DEL', KEYS[1]); end; return value",
            String.class
    );

    private final StringRedisTemplate redisTemplate;
    private final Duration expiration;
    private final SecureRandom random = new SecureRandom();

    public CaptchaServiceImpl(StringRedisTemplate redisTemplate,
                              @Value("${captcha.expiration:120s}") Duration expiration) {
        this.redisTemplate = redisTemplate;
        this.expiration = expiration;
    }

    @Override
    public CaptchaVO generate() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode(4);
        redisTemplate.opsForValue().set(KEY_PREFIX + captchaId, code, expiration);
        return new CaptchaVO(captchaId, renderImage(code));
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        String expected = redisTemplate.execute(GET_AND_DELETE_SCRIPT, List.of(KEY_PREFIX + captchaId));
        if (expected == null) {
            throw new BusinessException(400, "验证码已过期，请刷新后重试");
        }
        if (!expected.equals(captchaCode.trim().toUpperCase(Locale.ROOT))) {
            throw new BusinessException(400, "验证码错误");
        }
    }

    private String randomCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS[random.nextInt(CHARACTERS.length)]);
        }
        return code.toString();
    }

    private String renderImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(242, 247, 252));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 6; i++) {
                graphics.setColor(randomColor(150, 210));
                graphics.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(randomColor(30, 130));
                graphics.drawString(String.valueOf(code.charAt(i)), 10 + i * 27, 32 + random.nextInt(5));
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("验证码图片生成失败", e);
        }
    }

    private Color randomColor(int min, int max) {
        int range = max - min;
        return new Color(min + random.nextInt(range), min + random.nextInt(range), min + random.nextInt(range));
    }
}
