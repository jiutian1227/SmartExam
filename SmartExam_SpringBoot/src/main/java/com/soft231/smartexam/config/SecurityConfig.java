package com.soft231.smartexam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soft231.smartexam.common.Result;
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
 * 4. 每个Controller的每个端点都在下面显式声明角色；规则自上而下匹配，越具体的路径越靠前
 *
 * 角色缩写：TEACHER=教师(0) STUDENT=学生(1) SUPERADMIN=超级管理员(2)
 * 标注"Controller内校验"的接口表示角色不限（师生共用），但方法内部按JWT身份做归属/可见性校验
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
                // ══ 1. 公开接口：仅登录、注册、验证码 ══
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/captcha/**"
                ).permitAll()

                // ══ 2. 静态资源：验证码图片与头像（img标签无法携带Authorization头）══
                .requestMatchers(
                    "/captcha-images/**",
                    "/uploads/**"
                ).permitAll()

                // ══ 3. AuthController：登录注册已公开，退出与当前用户信息需登录态 ══
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                // ══ 4. UserController：用户管理 ══
                // 列表（含搜索）仅超管
                .requestMatchers(HttpMethod.GET, "/api/user").hasRole("SUPERADMIN")
                // 查、改本人资料放行给所有登录用户（Controller内强制只能查/改自己）
                .requestMatchers(HttpMethod.GET, "/api/user/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/user").authenticated()
                // 创建、删除用户仅超管
                .requestMatchers(HttpMethod.POST, "/api/user").hasRole("SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/user/*").hasRole("SUPERADMIN")

                // ══ 5. AvatarController：头像上传/删除，一律操作本人（userId取自JWT）══
                .requestMatchers(HttpMethod.POST, "/api/avatar/upload").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/avatar").authenticated()

                // ══ 6. UserGroupController：用户组 ══
                // 6.1 学生自助：加入、按分享码加入、退出、我的分组、按分享码查详情
                .requestMatchers(HttpMethod.POST,
                    "/api/user-group/join", "/api/user-group/join-by-code").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user-group/leave").authenticated()
                .requestMatchers(HttpMethod.GET,
                    "/api/user-group/my-groups", "/api/user-group/by-share-code/*").authenticated()
                // 6.2 只读：列表按creatorId过滤，详情与成员列表Controller内做可见性校验
                .requestMatchers(HttpMethod.GET,
                    "/api/user-group", "/api/user-group/*", "/api/user-group/*/members").authenticated()
                // 6.3 写操作：教师及以上（Controller内再校验是否为创建者）
                .requestMatchers(HttpMethod.POST,
                    "/api/user-group", "/api/user-group/*/member")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT,
                    "/api/user-group", "/api/user-group/*/share-code")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/user-group/*", "/api/user-group/*/member/*")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ══ 7. ExamController：考试管理 ══
                // 7.1 学生入口：可参加的考试列表，仅学生（SQL已限定其所属用户组）
                .requestMatchers(HttpMethod.GET, "/api/exams/available").hasRole("STUDENT")
                // 7.2 管理端：考试列表与统计，教师及以上
                .requestMatchers(HttpMethod.GET, "/api/exams").hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/exams/*/stats").hasAnyRole("TEACHER", "SUPERADMIN")
                // 7.3 考试详情与题目：师生共用，Controller内canAccess按角色判定可见性
                .requestMatchers(HttpMethod.GET,
                    "/api/exams/*", "/api/exams/*/questions").authenticated()
                // 7.4 考试及试卷题目的写操作：教师及以上
                .requestMatchers(HttpMethod.POST, "/api/exams/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/exams/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/exams/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ══ 8. QuestionController：题库（含答案与解析，学生一律不可见）══
                .requestMatchers(HttpMethod.GET,
                    "/api/question", "/api/question/*").hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/question/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/question/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/question/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ══ 9. KnowledgePointController：知识点属教学资源管理，教师及以上 ══
                .requestMatchers("/api/knowledge-point", "/api/knowledge-point/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ══ 10. AnnouncementController：公告 ══
                // 10.1 按角色取本人可见公告：所有登录用户
                .requestMatchers(HttpMethod.GET, "/api/announcement/role").authenticated()
                // 10.2 管理端全量列表与详情：教师及以上
                .requestMatchers(HttpMethod.GET,
                    "/api/announcement", "/api/announcement/*").hasAnyRole("TEACHER", "SUPERADMIN")
                // 10.3 公告写操作：教师及以上
                .requestMatchers(HttpMethod.POST, "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/announcement/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")

                // ══ 11. RecordController：考试记录与批阅 ══
                // 11.1 学生自助：开始、提交、存草稿、我的成绩、我的考试状态
                .requestMatchers(HttpMethod.POST,
                    "/api/records/start", "/api/records/submit", "/api/records/draft").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/records/my", "/api/records/my/**").authenticated()
                // 11.2 教师及以上：全量记录、批阅统计、按考试查记录、AI批主观题、批阅写操作
                .requestMatchers(HttpMethod.GET, "/api/records").hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET,
                    "/api/records/stats", "/api/records/stats/all").hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/records/by-exam/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/records/ai-grade-subjective")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST,
                    "/api/records/*/auto-grade", "/api/records/*/scores")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/records/**")
                    .hasAnyRole("TEACHER", "SUPERADMIN")
                // 11.3 记录详情与作答明细：师生共用，Controller内assertRecordVisible兜底
                .requestMatchers(HttpMethod.GET, "/api/records/*", "/api/records/*/answers").authenticated()
                // 11.4 新建记录：userId一律取JWT身份
                .requestMatchers(HttpMethod.POST, "/api/records").authenticated()

                // ══ 12. AI能力 ══
                // 题解生成：学生查看已批阅成绩时需要，故登录即可，Controller内限制输入长度防滥用
                .requestMatchers(HttpMethod.POST, "/api/solution/generate").authenticated()
                // AI答疑助手：面向全体登录用户的助学功能
                .requestMatchers(HttpMethod.POST, "/api/ai-assistant/ask-stream").authenticated()

                // ══ 13. 其余接口：登录即可（兜底，防止新增端点漏配后被匿名访问）══
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

    // 过滤器层没有Spring MVC的消息转换器，需自己序列化
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 过滤器层直接写回JSON响应
     * @param status  HTTP状态码
     * @param message 提示信息
     */
    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), Result.error(status, message));
    }
}
