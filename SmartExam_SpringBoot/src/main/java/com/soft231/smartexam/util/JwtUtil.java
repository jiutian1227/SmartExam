package com.soft231.smartexam.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 生成和解析JWT token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 启动自检：提醒默认密钥风险，避免把 yml 里写死的密钥直接带上线
     */
    @PostConstruct
    public void checkSecretStrength() {
        boolean fromEnv = System.getenv("JWT_SECRET") != null || System.getProperty("JWT_SECRET") != null;
        if (!fromEnv) {
            log.warn("正在使用 application.yml 中的默认 JWT 密钥，上线前必须通过环境变量 JWT_SECRET 覆盖");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT 密钥长度不足 32 字节，签名强度不足，建议更换为更长的随机串");
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT token
     *
     * 刻意不放 role：角色一律由 JwtAuthenticationFilter 回查数据库取当前值，
     * 放在 token 里会变成签发时刻的快照，管理员改权限后旧 token 收不回来。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT字符串
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析token，获取Claims，解析失败（过期/伪造）返回null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
