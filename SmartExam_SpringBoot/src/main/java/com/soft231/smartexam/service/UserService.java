package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.User;

import java.util.List;
import java.util.Map;

//用户服务接口
//创建用户
//更新用户信息
//删除用户
//用户登录
//用户注册
//搜索用户
public interface UserService extends IService<User> {

    //创建用户
    User createUser(User user);

    //更新用户信息
    User updateUser(User user);

    //删除用户
    boolean deleteUser(Long id);

    //用户登录
    Map<String, Object> login(String username, String password);

    //用户注册
    User register(User user);

    //搜索用户
    List<User> searchUsers(String keyword);
}
