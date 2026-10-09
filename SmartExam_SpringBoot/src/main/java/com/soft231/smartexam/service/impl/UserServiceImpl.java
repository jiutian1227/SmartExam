package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.mapper.UserMapper;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.BCryptUtil;
import com.soft231.smartexam.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.soft231.smartexam.service.CaptchaService captchaService;

    /** 连续登录失败达到该次数后锁定账号 */
    private static final int MAX_FAIL_COUNT = 99;
    /** 锁定持续时间：5分钟 */
    private static final long LOCK_DURATION_MILLIS = 5 * 60 * 1000L;

    /** 登录失败计数 */
    private final java.util.Map<String, LoginAttempt> loginAttempts = new java.util.concurrent.ConcurrentHashMap<>();

    //用户登录：先检查锁定，再消费验证码凭证，最后校验密码
    @Override
    public Map<String, Object> login(String username, String password, String captchaToken) {
        pruneLoginAttempts();

        // 1. 账号锁定检查
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null && attempt.lockedUntil > System.currentTimeMillis()) {
            long seconds = (attempt.lockedUntil - System.currentTimeMillis()) / 1000;
            throw new IllegalArgumentException("登录失败次数过多，请在 " + seconds + " 秒后重试");
        }

        // 2. 验证码凭证校验（一次性，防止绕过滑块直接爆破密码）
        if (!captchaService.consume(captchaToken)) {
            throw new IllegalArgumentException("验证码无效或已过期，请重新完成验证");
        }

        // 3. 密码校验
        User existingUser = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        boolean matched = existingUser != null
                && existingUser.getPassword() != null
                && BCryptUtil.matches(password, existingUser.getPassword());

        if (!matched) {
            // 用户不存在与密码错误返回同一提示，避免暴露用户名是否存在
            throw new IllegalArgumentException(recordFailure(username));
        }

        loginAttempts.remove(username);

        Map<String, Object> result = new HashMap<>();
        // token 只带身份标识，角色由拦截器每次回查数据库，保证改权限后旧 token 立即失效
        result.put("token", jwtUtil.generateToken(existingUser.getId(), existingUser.getUsername()));
        // 不将密码哈希返回给前端
        existingUser.setPassword(null);
        result.put("user", existingUser);
        return result;
    }

    /**
     * 记录一次登录失败，达到阈值则锁定账号
     * @return 给前端的提示语
     */
    private String recordFailure(String username) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.failCount++;
        attempt.lastFailAt = System.currentTimeMillis();

        if (attempt.failCount >= MAX_FAIL_COUNT) {
            attempt.lockedUntil = System.currentTimeMillis() + LOCK_DURATION_MILLIS;
            attempt.failCount = 0;
            return "登录失败次数过多，账号已锁定 5 分钟";
        }
        return "用户名或密码错误，还可尝试 " + (MAX_FAIL_COUNT - attempt.failCount) + " 次";
    }

    /** 清理已过期的失败记录，避免内存无限增长 */
    private void pruneLoginAttempts() {
        long now = System.currentTimeMillis();
        loginAttempts.entrySet().removeIf(e -> {
            LoginAttempt a = e.getValue();
            return a.lockedUntil < now && (now - a.lastFailAt) > LOCK_DURATION_MILLIS;
        });
    }

    private static class LoginAttempt {
        private int failCount;
        private long lastFailAt;
        private long lockedUntil;
    }

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

    //用户注册（校验顺序与登录一致：先核销验证码凭证，再查重落库，防止脚本批量注册）
    @Override
    public User register(User user, String captchaToken) {
        // 1. 验证码凭证校验（一次性，与登录同一套：不让绕过滑块直接调接口刷号）
        if (!captchaService.consume(captchaToken)) {
            throw new IllegalArgumentException("验证码无效或已过期，请重新完成验证");
        }

        // 2. 检查用户名是否已存在
        //    用 IllegalArgumentException 而非 RuntimeException：归到 400，
        //    与登录的"用户名或密码错误"同一口径，不会在日志里打成 500 系统错误
        User existing = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 3. 角色收口：只允许 0-教师 / 1-学生，其余（含超管 2）一律降级为学生，防止自注册提权
        if (user.getRole() == null || (user.getRole() != 0 && user.getRole() != 1)) {
            user.setRole(1);
        }

        // 4. 密码加密后入库
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