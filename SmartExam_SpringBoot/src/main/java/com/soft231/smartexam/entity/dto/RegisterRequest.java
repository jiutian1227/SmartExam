package com.soft231.smartexam.entity.dto;

import lombok.Data;

/**
 * 注册请求体
 *
 * 与 LoginRequest 保持同一套约定：captchaToken 为滑块验证码通过后签发的一次性凭证，
 * 服务端消费后立即失效。单独建 DTO 而不复用 User 实体，是为了避免把
 * "验证码凭证"这类传输字段混进实体（实体字段会被当成表字段参与 INSERT）。
 */
@Data
public class RegisterRequest {

    private String username;

    private String realName;

    private String password;

    /** 0=教师 1=学生；服务端会收口：非 0/1 一律降级为学生，防止自注册提权 */
    private Integer role;

    private String captchaToken;
}
