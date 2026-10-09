package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.Announcement;
import com.soft231.smartexam.service.AnnouncementService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//公告管理控制器
//公告CRUD：创建 create / 查询 getById / 查询全部 list / 更新 update / 删除 delete
//按角色过滤：按当前登录用户角色获取公告列表 getByRole
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    //按当前登录用户角色获取公告列表（角色取自JWT）
    @GetMapping("/role")
    public Result<List<Announcement>> getByRole() {
        Integer role = SecurityUtils.getRole();
        if (role == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(announcementService.getAnnouncementsByRole(role));
    }

    //获取所有公告列表（管理端全量）—— 仅教师及以上，SecurityConfig已限角色，此处纵深防御
    @GetMapping
    public Result<List<Announcement>> list() {
        if (!SecurityUtils.isTeacherOrAbove()) {
            throw new AccessDeniedException("无权查看公告管理列表");
        }
        return Result.success(announcementService.getAllAnnouncements());
    }

    //根据ID查询公告 —— 教师及以上可看全部；其余角色只能看targetRoles命中自己的公告
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        if (!SecurityUtils.isTeacherOrAbove()
                && !matchTargetRoles(announcement.getTargetRoles(), SecurityUtils.getRole())) {
            throw new AccessDeniedException("无权查看该公告");
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

    //删除公告 —— 仅教师及以上
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = announcementService.deleteAnnouncement(id);
        if (!deleted) {
            return Result.error("删除失败，公告不存在");
        }
        return Result.success();
    }

    /**
     * 判断公告的目标角色是否命中当前用户角色：
     * targetRoles为空表示全体可见；多个角色以逗号分隔，与user.role取值一致（0教师 1学生 2超管）
     */
    private boolean matchTargetRoles(String targetRoles, Integer role) {
        if (role == null) {
            return false;
        }
        if (targetRoles == null || targetRoles.trim().isEmpty()) {
            return true;
        }
        for (String part : targetRoles.split(",")) {
            if (part.trim().equals(String.valueOf(role))) {
                return true;
            }
        }
        return false;
    }
}