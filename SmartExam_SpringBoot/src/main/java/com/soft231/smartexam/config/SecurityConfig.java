package com.soft231.smartexam.config;

import com.soft231.smartexam.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 注册JWT过滤器，配置接口访问权限
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${auth.enabled:true}")
    private boolean authEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (authEnabled) {
            http
                .authorizeHttpRequests(auth -> auth
                    // 白名单 — 无需登录
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/captcha/generate",
                        "/api/captcha/verify",
                        "/api/ai-assistant/**",
                        "/captcha-images/**",
                        "/uploads/**"
                    ).permitAll()
                    // 其余所有请求需要登录
                    .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        }

        return http.build();
    }
}
