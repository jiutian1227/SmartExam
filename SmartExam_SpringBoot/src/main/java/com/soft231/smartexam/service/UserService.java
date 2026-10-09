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

    /**
     * 用户登录
     * @param username     用户名
     * @param password     明文密码
     * @param captchaToken 滑块验证码通过后签发的一次性凭证
     */
    Map<String, Object> login(String username, String password, String captchaToken);

    //用户注册（与登录一致：必须携带滑块验证码凭证，服务端核销后才落库）
    User register(User user, String captchaToken);

    //搜索用户
    List<User> searchUsers(String keyword);
}
