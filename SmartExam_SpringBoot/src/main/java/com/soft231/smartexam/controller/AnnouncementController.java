package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.Announcement;
import com.soft231.smartexam.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//公告管理控制器
//公告CRUD：创建 create / 查询 getById / 查询全部 list / 更新 update / 删除 delete
//按角色过滤：根据不同用户角色获取公告列表 getByRole
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    //根据用户角色获取公告列表
    @GetMapping("/role/{role}")
    public Result<List<Announcement>> getByRole(@PathVariable Integer role) {
        return Result.success(announcementService.getAnnouncementsByRole(role));
    }

    //获取所有公告列表
    @GetMapping
    public Result<List<Announcement>> list() {
        return Result.success(announcementService.getAllAnnouncements());
    }

    //根据ID查询公告
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    //创建公告
    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        return Result.success(announcementService.createAnnouncement(announcement));
    }

    //更新公告
    @PutMapping
    public Result<Announcement> update(@RequestBody Announcement announcement) {
        return Result.success(announcementService.updateAnnouncement(announcement));
    }

    //删除公告
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = announcementService.deleteAnnouncement(id);
        if (!deleted) {
            return Result.error("删除失败，公告不存在");
        }
        return Result.success();
    }
}