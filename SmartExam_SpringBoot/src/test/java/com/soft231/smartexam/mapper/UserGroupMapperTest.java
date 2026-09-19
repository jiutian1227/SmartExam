package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.UserGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserGroupMapperTest {

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Test
    public void insertTest() {
        UserGroup group = new UserGroup();
        group.setName("计算机科学与技术班");
        group.setDescription("2024级计科班");
        group.setShareCode("CS2024");
        group.setCreatorId(1L);
        int result = userGroupMapper.insert(group);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + group.getId());
    }

    @Test
    public void updateTest() {
        UserGroup group = new UserGroup();
        group.setName("待更新组");
        group.setDescription("待更新描述");
        group.setCreatorId(1L);
        userGroupMapper.insert(group);
        System.out.println("插入ID: " + group.getId());

        UserGroup updateGroup = new UserGroup();
        updateGroup.setId(group.getId());
        updateGroup.setName("已更新的组名");
        updateGroup.setDescription("已更新的描述信息");
        int result = userGroupMapper.updateById(updateGroup);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        UserGroup group = new UserGroup();
        group.setName("待删除组");
        group.setCreatorId(1L);
        userGroupMapper.insert(group);
        System.out.println("待删除ID: " + group.getId());

        int result = userGroupMapper.deleteById(group.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        UserGroup group = userGroupMapper.selectById(1L);
        System.out.println(group);
    }

    @Test
    public void selectAllTest() {
        List<UserGroup> list = userGroupMapper.selectList(null);
        for (UserGroup g : list) {
            System.out.println(g);
        }
    }


    @Test
    public void selectGroupsWithMemberCountTest() {
        List<?> list = userGroupMapper.selectGroupsWithMemberCount();
        System.out.println("所有用户组(含成员数):");
        for (Object o : list) {
            System.out.println(o);
        }
    }

    @Test
    public void selectGroupsWithMemberCountByCreatorIdTest() {
        List<?> list = userGroupMapper.selectGroupsWithMemberCountByCreatorId(1L);
        System.out.println("创建者1的用户组(含成员数):");
        for (Object o : list) {
            System.out.println(o);
        }
    }

    @Test
    public void selectGroupWithMemberCountByIdTest() {
        Object vo = userGroupMapper.selectGroupWithMemberCountById(1L);
        System.out.println("组1详情(含成员数): " + vo);
    }
}
