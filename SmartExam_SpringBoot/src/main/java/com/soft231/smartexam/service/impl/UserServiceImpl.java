package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.mapper.UserMapper;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.BCryptUtil;
import com.soft231.smartexam.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//用户服务实现类
//用户账户：创建用户 createUser / 更新信息 updateUser / 删除用户 deleteUser
//认证功能：用户登录 login / 用户注册 register
//搜索功能：搜索用户 searchUsers
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    //创建用户
    @Override
    public User createUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!BCryptUtil.isEncoded(user.getPassword())) {
                user.setPassword(BCryptUtil.encode(user.getPassword()));
            }
        }
        this.save(user);
        return user;
    }

    //更新用户信息
    @Override
    public User updateUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            User existing = this.getById(user.getId());
            if (existing != null) {
                user.setPassword(existing.getPassword());
            }
        } else {
            if (!BCryptUtil.isEncoded(user.getPassword())) {
                user.setPassword(BCryptUtil.encode(user.getPassword()));
            }
        }
        this.updateById(user);
        return user;
    }

    //删除用户
    @Override
    public boolean deleteUser(Long id) {
        log.info("删除用户, id: {}", id);
        boolean result = this.removeById(id);
        log.info("删除用户完成");
        return result;
    }

    //用户登录
    @Override
    public Map<String, Object> login(String username, String password) {
        User existingUser = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        
        if (existingUser != null && BCryptUtil.matches(password, existingUser.getPassword())) {
            Map<String, Object> result = new HashMap<>();
            result.put("token", JwtUtil.generateToken(existingUser.getId(), existingUser.getUsername(), existingUser.getRole()));
            result.put("user", existingUser);
            return result;
        }
        return null;
    }

    //用户注册
    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        User existing = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (user.getRole() == null || (user.getRole() != 0 && user.getRole() != 1)) {
            user.setRole(1);
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCryptUtil.encode(user.getPassword()));
        }
        this.save(user);
        return user;
    }

    //搜索用户
    @Override
    public List<User> searchUsers(String keyword) {
        return this.lambdaQuery()
                .and(wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword))
                .list();
    }
}