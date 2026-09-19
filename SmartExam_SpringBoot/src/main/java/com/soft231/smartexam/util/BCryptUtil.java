package com.soft231.smartexam.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
public class BCryptUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * BCrypt加密密码的前缀
     */
    private static final String BCRYPT_PREFIX = "$2";

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 判断密码是否已经是BCrypt加密过的
     * BCrypt加密后的密码格式: $2a$或$2b$或$2y$开头，长度固定为60
     * @param password 待检测的密码
     * @return 是否已加密
     */
    public static boolean isEncoded(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.startsWith(BCRYPT_PREFIX) && password.length() == 60;
    }
}