package com.soft231.smartexam.service.impl;

import com.soft231.smartexam.service.CaptchaService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现
 * 待验证状态与已通过凭证都带有效期并定期清理，避免内存无限增长
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String IMAGE_DIR = "captcha-images";
    private static final int SHAPE_COUNT = 6;
    /** 允许的拼图位置误差（像素） */
    private static final int TOLERANCE = 10;
    /** 验证码有效期：5分钟 */
    private static final long TTL_MILLIS = 5 * 60 * 1000L;

    /** token -> 拼图正确位置 + 过期时间 */
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();
    /** token -> 凭证过期时间（已通过校验，等待登录时消费） */
    private final Map<String, Long> verified = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public Map<String, Object> generate() {
        prune();

        File dir = new File(IMAGE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (files == null || files.length == 0) {
            throw new IllegalStateException("验证码素材缺失");
        }

        Random random = new Random();
        String imageName = files[random.nextInt(files.length)];
        int gapX = random.nextInt(215) + 10;
        String token = UUID.randomUUID().toString();

        pending.put(token, new Pending(gapX, System.currentTimeMillis() + TTL_MILLIS));

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("imageUrl", "/captcha-images/" + imageName);
        result.put("gapX", gapX);
        result.put("shapeIndex", random.nextInt(SHAPE_COUNT));
        return result;
    }

    @Override
    public boolean verify(String token, Integer userX) {
        if (token == null || token.isBlank() || userX == null) {
            return false;
        }
        prune();

        Pending item = pending.get(token);
        if (item == null) {
            return false;
        }
        if (Math.abs(userX - item.gapX) > TOLERANCE) {
            return false;
        }
        // 通过后从待验证集合移入已通过集合，签发一次性凭证
        pending.remove(token);
        verified.put(token, System.currentTimeMillis() + TTL_MILLIS);
        return true;
    }

    @Override
    public boolean consume(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        prune();
        return verified.remove(token) != null;
    }

    /** 清理已过期的待验证项与凭证 */
    private void prune() {
        long now = System.currentTimeMillis();
        pending.entrySet().removeIf(e -> e.getValue().expireAt < now);
        verified.entrySet().removeIf(e -> e.getValue() < now);
    }

    private static class Pending {
        private final int gapX;
        private final long expireAt;

        Pending(int gapX, long expireAt) {
            this.gapX = gapX;
            this.expireAt = expireAt;
        }
    }
}
