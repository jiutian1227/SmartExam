package com.soft231.smartexam.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 生成和解析JWT token
 */
public class JwtUtil {

    private static final String SECRET = "SmartExamSecretKey2026!@#$%%^&*()VeryLongEnough";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT token
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT字符串
     */
    public static String generateToken(Long userId, String username, Integer role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析token，获取Claims
     * @param token JWT字符串
     * @return Claims，解析失败返回null
     */
    public static Claims parseToken(String token) {
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

    /**
     * 从token中获取userId（兼容旧格式 token_xxx）
     */
    public static Long getUserId(String token) {
        if (token == null) return null;
        // 兼容旧格式
        if (token.startsWith("token_")) {
            try {
                return Long.parseLong(token.substring(6));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // JWT格式
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从token中获取角色
     */
    public static Integer getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", Integer.class) : null;
    }

    /**
     * 校验token是否有效
     */
    public static boolean validateToken(String token) {
        if (token == null) return false;
        if (token.startsWith("token_")) return true;
        return parseToken(token) != null;
    }
}
