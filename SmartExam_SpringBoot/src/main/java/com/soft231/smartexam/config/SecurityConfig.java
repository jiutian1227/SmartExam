package com.soft231.smartexam.config;

import com.soft231.smartexam.filter.JwtAuthenticationFilter;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Spring Security配置类
 * 注册JWT过滤器，配置接口访问权限
 * 角色：0=教师(TEACHER) 1=学生(STUDENT) 2=超级管理员(SUPERADMIN)
 *
 * 鉴权原则：
 * 1. 除登录、注册、验证码外，所有业务接口必须携带有效JWT（anyRequest().authenticated()兜底）
 * 2. 静态资源（验证码图片、头像）无法在img标签上附带请求头，维持公开
 * 3. 涉及"本人数据"的接口在Controller内再按JWT身份做归属校验，杜绝水平越权
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil, UserService userService) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── 公开接口：仅登录、注册、验证码 ──
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/captcha/**"
                ).permitAll()

                // ── 静态资源：验证码图片与头像（img标签无法携带Authorization头）──
                .requestMatchers(
                    "/captcha-images/**",
                    "/uploads/**"
                ).permitAll()

                // ── 用户管理：创建/删除/列表/搜索仅超级管理员 ──
                // 查、改本人资料放行给所有登录用户（Controller内强制只能改自己）
                .requestMatchers(HttpMethod.GET, "/api/user/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/user").authenticated()
                .requestMatchers("/api/user/**").hasRole("SUPERADMIN")

                // ── 用户组：学生自助操作，需先于教师通配规则声明 ──
                .requestMatchers(HttpMethod.POST,
                    "/api/user-group/join", "/api/user-group/join-by-code").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user-group/leave").authenticated()
                .requestMatchers(HttpMethod.GET,
                    "/api/user-group/my-groups", "/api/user-group/by-share-code/*").authenticated()
                .requestMatchers(HttpMethod.GET,
                    "/api/user-group", "/api/user-group/*", "/api/user-group/*/members").authenticated()

                // ── 用户组写操作：教师及以上 ──
                .requestMatchers(HttpMethod.POST, "/api/user-group", "/api/user-group/*/member")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/user-group/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/user-group/*", "/api/user-group/*/member/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ── 学生考试入口：仅学生，且SQL已限定只返回其所属用户组的试卷 ──
                .requestMatchers(HttpMethod.GET, "/api/exams/available").hasRole("STUDENT")

                // ── 公告：全量列表用于管理端，学生只能走 /role 取自己可见的公告 ──
                .requestMatchers(HttpMethod.GET, "/api/announcement")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ── 考试/题目/知识点/公告：写操作教师及以上 ──
                .requestMatchers(HttpMethod.POST,
                    "/api/exams/**", "/api/question/**", "/api/knowledge-point/**", "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT,
                    "/api/exams/**", "/api/question/**", "/api/knowledge-point/**", "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/exams/**", "/api/question/**", "/api/knowledge-point/**", "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ── AI智能出题：教师及以上 ──
                .requestMatchers(HttpMethod.POST, "/api/question/ai-generate")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ── 考试记录：学生自助（开始/提交/草稿/我的成绩/我的状态）──
                .requestMatchers(HttpMethod.POST,
                    "/api/records/start", "/api/records/submit", "/api/records/draft").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/records/my", "/api/records/my/**").authenticated()

                // ── 批阅、全量记录与统计：教师及以上 ──
                .requestMatchers(
                    "/api/records/stats",
                    "/api/records/ai-grade-subjective",
                    "/api/records/by-exam/**"
                ).hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/records/*/auto-grade", "/api/records/*/scores")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/records")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/records/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/exams/*/stats")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ── 其余接口：登录即可 ──
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        writeJson(res, 401, "未登录或登录已过期，请重新登录"))
                .accessDeniedHandler((req, res, ex) ->
                        writeJson(res, 403, "无权限执行此操作"))
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
