package com.soft231.smartexam.service;

import java.util.Map;

/**
 * 验证码服务
 * 状态流转：generate(生成待验证) → verify(验证通过，签发一次性凭证) → consume(登录时消费)
 * 凭证一次性且带有效期，杜绝验证码只做前端动画、可被绕过的问题
 */
public interface CaptchaService {

    /**
     * 生成滑块验证码
     * @return token / imageUrl / gapX / shapeIndex
     */
    Map<String, Object> generate();

    /**
     * 校验滑块位置，通过后签发一次性凭证
     */
    boolean verify(String token, Integer userX);

    /**
     * 消费凭证（登录时使用）：一次性，消费后立即失效
     */
    boolean consume(String token);
}
