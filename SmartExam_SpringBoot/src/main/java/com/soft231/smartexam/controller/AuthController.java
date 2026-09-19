package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//认证控制器
//用户登录 login
//用户注册 register
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    //用户登录
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        Map<String, Object> result = userService.login(user.getUsername(), user.getPassword());
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("用户名或密码错误");
    }

    //用户注册
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User saved = userService.register(user);
        return Result.success(saved);
    }
}
