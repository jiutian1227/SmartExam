package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//用户管理控制器
//用户CRUD：创建 create / 查询 getById / 更新 update / 删除 delete / 列表 list
//搜索功能：关键词搜索用户列表 searchUsers
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //创建用户
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        User saved = userService.createUser(user);
        return Result.success(saved);
    }

    //根据ID查询用户
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    //更新用户信息
    @PutMapping
    public Result<User> update(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success(user);
    }

    //删除用户
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    //获取用户列表，支持关键词搜索
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return Result.success(userService.searchUsers(keyword.trim()));
        }
        return Result.success(userService.list());
    }
}
