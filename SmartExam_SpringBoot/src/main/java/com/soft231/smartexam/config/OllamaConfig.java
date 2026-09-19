package com.soft231.smartexam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Ollama配置类
 * 从application.yml读取ollama相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {
    
    /**
     * Ollama服务地址
     */
    private String url = "http://localhost:11434";
    
    /**
     * 使用的AI模型名称
     */
    private String model = "gamma3:4b";
}