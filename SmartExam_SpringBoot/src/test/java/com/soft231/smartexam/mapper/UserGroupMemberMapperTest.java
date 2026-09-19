package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.UserGroupMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserGroupMemberMapperTest {

    @Autowired
    private UserGroupMemberMapper userGroupMemberMapper;

    @Test
    public void insertTest() {
        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(1L);
        member.setUserId(1L);
        member.setJoinTime(LocalDateTime.now());
        int result = userGroupMemberMapper.insert(member);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + member.getId());
    }

    @Test
    public void updateTest() {
        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(1L);
        member.setUserId(2L);
        member.setJoinTime(LocalDateTime.now());
        userGroupMemberMapper.insert(member);
        System.out.println("插入ID: " + member.getId());

        UserGroupMember updateMember = new UserGroupMember();
        updateMember.setId(member.getId());
        updateMember.setJoinTime(LocalDateTime.now().plusDays(1));
        int result = userGroupMemberMapper.updateById(updateMember);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(1L);
        member.setUserId(3L);
        member.setJoinTime(LocalDateTime.now());
        userGroupMemberMapper.insert(member);
        System.out.println("待删除ID: " + member.getId());

        int result = userGroupMemberMapper.deleteById(member.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        UserGroupMember member = userGroupMemberMapper.selectById(1L);
        System.out.println(member);
    }

    @Test
    public void selectAllTest() {
        List<UserGroupMember> list = userGroupMemberMapper.selectList(null);
        for (UserGroupMember m : list) {
            System.out.println(m);
        }
    }


    @Test
    public void selectMembersWithUserByGroupIdTest() {
        List<?> list = userGroupMemberMapper.selectMembersWithUserByGroupId(1L);
        System.out.println("组1的成员(含用户信息):");
        for (Object o : list) {
            System.out.println(o);
        }
    }
}
