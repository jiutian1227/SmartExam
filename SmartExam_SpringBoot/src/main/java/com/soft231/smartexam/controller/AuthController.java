package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.entity.dto.LoginRequest;
import com.soft231.smartexam.entity.dto.RegisterRequest;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//认证控制器
//用户登录 login
//用户注册 register
//退出登录 logout（需携带JWT，身份一律从token解析）
//当前登录用户信息 me（需携带JWT，用于在刷新页面后按token还原用户身份）
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    //用户登录（必须携带滑块验证码通过后签发的一次性凭证）
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = userService.login(
                request.getUsername(), request.getPassword(), request.getCaptchaToken());
        return Result.success(result);
    }

    //用户注册（与登录一致：必须携带滑块验证码通过后签发的一次性凭证）
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        User saved = userService.register(user, request.getCaptchaToken());
        return Result.success(saved);
    }

    //退出登录
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT无状态，服务端无需失效token，前端清除本地token即可
        return Result.success();
    }

    //获取当前登录用户信息（身份从JWT解析，不信任任何入参）
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Long userId = SecurityUtils.getUserId();
        Integer role = SecurityUtils.getRole();
        if (userId == null || role == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", role);
        data.put("avatar", user.getAvatar());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        return Result.success(data);
    }
}
