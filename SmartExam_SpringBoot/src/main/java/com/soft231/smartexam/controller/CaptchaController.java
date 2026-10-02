package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 验证码控制器
 * 获取验证码图片列表 getImages
 * 生成验证码 generate
 * 验证验证码 verify —— 通过后由 CaptchaService 签发一次性凭证，登录接口必须携带该凭证
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    //获取验证码图片列表
    @GetMapping("/images")
    public Result<List<String>> getImages() {
        File dir = new File("captcha-images");
        String[] files = dir.list((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        List<String> imageNames = files != null ? Arrays.asList(files) : Collections.emptyList();
        return Result.success(imageNames.stream().map(name -> "/captcha-images/" + name).toList());
    }

    //生成验证码
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate() {
        return Result.success(captchaService.generate());
    }

    //验证验证码（通过后签发一次性凭证，供登录接口消费）
    @PostMapping("/verify")
    public Result<Boolean> verify(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("token");
        Integer userX = body.get("userX") == null ? null : ((Number) body.get("userX")).intValue();
        return Result.success(captchaService.verify(token, userX));
    }
}
