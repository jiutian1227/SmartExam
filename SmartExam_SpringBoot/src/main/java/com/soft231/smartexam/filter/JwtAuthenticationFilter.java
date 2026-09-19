package com.soft231.smartexam.filter;

import com.soft231.smartexam.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

//JWT鉴权过滤器
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //白名单路径
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/captcha/generate",
            "/api/captcha/verify",
            "/api/ai-assistant/",
            "/captcha-images/",
            "/uploads/"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 白名单直接放行
        for (String whitePath : WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 从请求头获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return;
        }

        String token = authHeader.substring(7);
        Long userId = JwtUtil.getUserId(token);

        if (userId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return;
        }

        // 将userId设置到SecurityContext中，后续可以通过SecurityContextHolder获取
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
