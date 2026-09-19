package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Announcement;
import com.soft231.smartexam.mapper.AnnouncementMapper;
import com.soft231.smartexam.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//公告服务实现类
//公告CRUD：创建 createAnnouncement / 更新 updateAnnouncement / 删除 deleteAnnouncement
//查询功能：按角色查询 getAnnouncementsByRole / 查询全部 getAllAnnouncements
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    //根据用户角色获取公告列表
    @Override
    public List<Announcement> getAnnouncementsByRole(Integer role) {
        return baseMapper.selectByRole(role);
    }

    //获取所有公告列表
    @Override
    public List<Announcement> getAllAnnouncements() {
        return baseMapper.selectAll();
    }

    //创建公告
    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        this.save(announcement);
        return announcement;
    }

    //更新公告
    @Override
    public Announcement updateAnnouncement(Announcement announcement) {
        announcement.setUpdateTime(LocalDateTime.now());
        this.updateById(announcement);
        return announcement;
    }

    //删除公告
    @Override
    public boolean deleteAnnouncement(Long id) {
        return this.removeById(id);
    }
}