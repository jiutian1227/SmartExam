package com.soft231.smartexam.entity.dto;

import lombok.Data;

/**
 * 登录请求体
 * captchaToken 为滑块验证码通过后签发的一次性凭证，服务端消费后立即失效
 */
@Data
public class LoginRequest {

    private String username;

    private String password;

    private String captchaToken;
}
