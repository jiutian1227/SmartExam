package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void insertTest() {
        User user = new User();
        user.setUsername("test_user");
        user.setPassword("123456");
        user.setRealName("测试用户");
        user.setRole(1);
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        int result = userMapper.insert(user);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + user.getId());
    }

    @Test
    public void updateTest() {
        // 先插入
        User user = new User();
        user.setUsername("update_test");
        user.setPassword("123456");
        user.setRealName("待更新");
        userMapper.insert(user);
        System.out.println("插入ID: " + user.getId());

        // 执行更新
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setRealName("更新后的名字");
        updateUser.setEmail("updated@example.com");
        int result = userMapper.updateById(updateUser);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        // 先插入
        User user = new User();
        user.setUsername("del_user");
        user.setPassword("123456");
        user.setRealName("待删除用户");
        userMapper.insert(user);
        System.out.println("待删除用户ID: " + user.getId());

        // 执行删除
        int result = userMapper.deleteById(user.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        User user = userMapper.selectById(1L);
        System.out.println(user);
    }

    @Test
    public void selectAllTest() {
        List<User> users = userMapper.selectList(null);
        for (User u : users) {
            System.out.println(u);
        }
    }
}
