package com.soft231.smartexam.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具类
 * 身份一律从JWT过滤器写入的SecurityContext中获取，禁止信任前端传参
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未认证时返回null
     */
    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 获取当前登录用户角色（0=教师 1=学生 2=超级管理员）
     * @return 角色，未认证时返回null
     */
    public static Integer getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> Role.fromAuthority(a))
                .filter(r -> r != null)
                .findFirst()
                .map(Role::getCode)
                .orElse(null);
    }

    /**
     * 当前用户是否为教师或超级管理员
     */
    public static boolean isTeacherOrAbove() {
        Integer role = getRole();
        return role != null && (role == Role.TEACHER.getCode() || role == Role.SUPER_ADMIN.getCode());
    }

    /**
     * 判断传入的用户ID是否为当前登录用户本人
     */
    public static boolean isSelf(Long userId) {
        Long current = getUserId();
        return current != null && current.equals(userId);
    }

    /**
     * 当前用户是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        Integer role = getRole();
        return role != null && role == Role.SUPER_ADMIN.getCode();
    }

    /**
     * 当前用户是否为学生
     */
    public static boolean isStudent() {
        Integer role = getRole();
        return role != null && role == Role.STUDENT.getCode();
    }

    /**
     * 角色枚举：与数据库user.role字段及前端约定一致
     */
    public enum Role {
        TEACHER(0, "ROLE_TEACHER"),
        STUDENT(1, "ROLE_STUDENT"),
        SUPER_ADMIN(2, "ROLE_SUPERADMIN");

        private final int code;
        private final String authority;

        Role(int code, String authority) {
            this.code = code;
            this.authority = authority;
        }

        public int getCode() {
            return code;
        }

        public String getAuthority() {
            return authority;
        }

        public static Role fromAuthority(String authority) {
            for (Role role : values()) {
                if (role.authority.equals(authority)) {
                    return role;
                }
            }
            return null;
        }
    }
}
