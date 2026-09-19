package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//验证码控制器
//获取验证码图片列表 getImages
//生成验证码 generate
//验证验证码 verify
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private static final String IMAGE_DIR = "captcha-images";
    private static final Map<String, Integer> captchaStore = new ConcurrentHashMap<>();
    private static final int SHAPE_COUNT = 6;

    static {
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    //获取验证码图片列表
    @GetMapping("/images")
    public Result<List<String>> getImages() {
        File dir = new File(IMAGE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        List<String> imageNames = files != null ? Arrays.asList(files) : Collections.emptyList();
        List<String> imageUrls = imageNames.stream()
                .map(name -> "/captcha-images/" + name)
                .toList();
        return Result.success(imageUrls);
    }

    //生成验证码
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate() {
        File dir = new File(IMAGE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        
        if (files == null || files.length == 0) {
            return Result.error("文件缺失");
        }

        String imageName = files[new Random().nextInt(files.length)];
        int gapX = new Random().nextInt(215) + 10;
        String token = UUID.randomUUID().toString();
        int shapeIndex = new Random().nextInt(SHAPE_COUNT);
        
        captchaStore.put(token, gapX);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("imageUrl", "/captcha-images/" + imageName);
        result.put("gapX",
                gapX);
        result.put("shapeIndex", shapeIndex);

        return Result.success(result);
    }

    //验证验证码
    @PostMapping("/verify")
    public Result<Boolean> verify(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("token");
        Integer userX = ((Number) body.get("userX")).intValue();

        Integer correctX = captchaStore.get(token);
        if (correctX == null) {
            return Result.error("验证码已过期");
        }

        captchaStore.remove(token);
        int diff = Math.abs(userX - correctX);

        return Result.success(diff <= 10);
    }
}