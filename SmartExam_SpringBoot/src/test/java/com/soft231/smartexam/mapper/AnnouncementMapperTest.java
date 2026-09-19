package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.Announcement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class AnnouncementMapperTest {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Test
    public void insertTest() {
        Announcement announcement = new Announcement();
        announcement.setTitle("系统维护通知");
        announcement.setContent("系统将于本周六凌晨2:00-4:00进行维护升级");
        announcement.setType("system");
        announcement.setTargetRoles("1,2");
        int result = announcementMapper.insert(announcement);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + announcement.getId());
    }

    @Test
    public void updateTest() {
        Announcement announcement = new Announcement();
        announcement.setTitle("待更新公告");
        announcement.setContent("这是待更新的内容");
        announcement.setType("info");
        announcementMapper.insert(announcement);
        System.out.println("插入ID: " + announcement.getId());

        Announcement updateAnn = new Announcement();
        updateAnn.setId(announcement.getId());
        updateAnn.setTitle("已更新公告标题");
        updateAnn.setContent("已更新的公告内容");
        int result = announcementMapper.updateById(updateAnn);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        Announcement announcement = new Announcement();
        announcement.setTitle("待删除公告");
        announcement.setContent("这条公告将被删除");
        announcement.setType("test");
        announcementMapper.insert(announcement);
        System.out.println("待删除ID: " + announcement.getId());

        int result = announcementMapper.deleteById(announcement.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        Announcement announcement = announcementMapper.selectById(1L);
        System.out.println(announcement);
    }

    @Test
    public void selectAllTest() {
        List<Announcement> list = announcementMapper.selectList(null);
        for (Announcement a : list) {
            System.out.println(a);
        }
    }


    @Test
    public void selectByRoleTest() {
        List<Announcement> list = announcementMapper.selectByRole(1);
        System.out.println("角色1可见的公告:");
        for (Announcement a : list) {
            System.out.println(a);
        }
    }

    @Test
    public void selectAllCustomTest() {
        List<Announcement> list = announcementMapper.selectAll();
        System.out.println("所有公告(自定义查询):");
        for (Announcement a : list) {
            System.out.println(a);
        }
    }
}
