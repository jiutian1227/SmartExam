package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.Announcement;

import java.util.List;

//公告服务接口
//根据用户角色获取公告列表
//获取所有公告列表
//创建公告
//更新公告
//删除公告
public interface AnnouncementService extends IService<Announcement> {

    //根据用户角色获取公告列表
    List<Announcement> getAnnouncementsByRole(Integer role);

    //获取所有公告列表
    List<Announcement> getAllAnnouncements();

    //创建公告
    Announcement createAnnouncement(Announcement announcement);

    //更新公告
    Announcement updateAnnouncement(Announcement announcement);

    //删除公告
    boolean deleteAnnouncement(Long id);
}