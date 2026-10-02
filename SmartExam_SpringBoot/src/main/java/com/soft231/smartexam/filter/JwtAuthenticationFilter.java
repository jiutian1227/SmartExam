package com.soft231.smartexam.filter;

import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.JwtUtil;
import com.soft231.smartexam.util.SecurityUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT鉴权过滤器
 *
 * token 只用来确认"你是谁"（userId，且必须验签通过）；
 * "你有什么权限"（role）一律回查数据库取当前值。
 *
 * 为什么角色不直接取 token 里的 claim：
 * token 里的角色是签发那一刻的快照。管理员把某人降级、或删除/封禁账号之后，
 * 旧 token 在过期之前（本项目 24 小时）依然带着旧角色，权限收不回来。
 * 改为每次请求回查 user 表的代价是"每个已登录请求多一次主键查询"，
 * 换来的是改权限立即生效、删号立即失效。
 *
 * 未携带/无效 token 时不设置认证信息，由 SecurityConfig 的 EntryPoint 返回 401。
 * 是否放行由 SecurityConfig 的 authorizeHttpRequests 统一决定，白名单只维护一处。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 未携带token：不设置认证信息，由SecurityConfig的EntryPoint返回401
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims != null ? claims.get("userId", Long.class) : null;

        if (userId == null) {
            // token无效或已过期：不设置认证信息，由SecurityConfig的EntryPoint返回401
            filterChain.doFilter(request, response);
            return;
        }

        // 角色以数据库当前值为准：账号被删除、被降级、被封禁都会在这里立即失效
        User user = userService.getById(userId);
        Integer role = user != null ? user.getRole() : null;

        SecurityUtils.Role roleEnum = null;
        if (role != null) {
            for (SecurityUtils.Role r : SecurityUtils.Role.values()) {
                if (r.getCode() == role) {
                    roleEnum = r;
                    break;
                }
            }
        }
        if (roleEnum == null) {
            // 用户不存在或角色非法：不设置认证信息，等同未登录
            filterChain.doFilter(request, response);
            return;
        }

        // 将userId与当前角色写入SecurityContext，后续通过SecurityUtils获取
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority(roleEnum.getAuthority())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
