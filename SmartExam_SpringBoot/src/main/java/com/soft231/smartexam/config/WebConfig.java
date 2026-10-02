package com.soft231.smartexam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 头像存储目录，与 AvatarController 共用 application.yml 的 upload.avatar-path，
     * 避免两处硬编码不一致导致"上传成功但访问 404"
     */
    @Value("${upload.avatar-path:./uploads/avatars}")
    private String avatarPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/captcha-images/**")
                .addResourceLocations("file:./captcha-images/");
        // 注意：handler 必须是 /uploads/avatars/** 而不是 /uploads/**。
        // 若用 /uploads/**，访问 /uploads/avatars/xx.png 时相对路径是 avatars/xx.png，
        // 会拼成 {avatarPath}/avatars/xx.png 多一层目录，导致上传成功却 404。
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarPath + "/");
    }

    // 跨域统一由 CorsConfig 的 CorsFilter 处理，此处不再重复配置，避免两套规则互相覆盖
}
