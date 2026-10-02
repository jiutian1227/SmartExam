package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.entity.UserGroup;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.service.UserGroupService;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;
import com.soft231.smartexam.entity.vo.UserGroupVO;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//用户组管理控制器
//群组CRUD：创建 create / 详情 getById / 更新 update / 删除 delete / 列表 list
//成员管理：获取成员列表 getMembers / 添加成员 addMember / 移除成员 removeMember
//加入离开：加入群组 join / 离开群组 leave / 通过分享码加入 joinByShareCode / 已加入群组列表 getMyGroups
//分享码管理：刷新分享码 refreshShareCode / 根据分享码查询 getByShareCode
@RestController
@RequestMapping("/api/user-group")
public class UserGroupController {

    @Autowired
    private UserGroupService userGroupService;

    //创建用户组
    @PostMapping
    public Result<UserGroup> create(@RequestBody UserGroup userGroup) {
        // creatorId以JWT身份为准，不信任前端传参
        userGroup.setCreatorId(SecurityUtils.getUserId());
        UserGroup saved = userGroupService.createGroup(userGroup);
        return Result.success(saved);
    }

    //根据ID查询用户组详情 —— 仅创建者、成员、超级管理员可见
    @GetMapping("/{id}")
    public Result<UserGroupVO> getById(@PathVariable Long id) {
        assertGroupVisible(id);
        UserGroupVO vo = userGroupService.getGroupDetail(id);
        if (vo == null) {
            return Result.error("用户组不存在");
        }
        return Result.success(vo);
    }

    //更新用户组信息 —— 教师只能改自己创建的组，超管不限
    @PutMapping
    public Result<UserGroup> update(@RequestBody UserGroup userGroup) {
        if (!SecurityUtils.isSuperAdmin()) {
            UserGroup existing = userGroupService.getById(userGroup.getId());
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能修改自己创建的用户组");
            }
            userGroup.setCreatorId(existing.getCreatorId());
        }
        userGroupService.updateById(userGroup);
        return Result.success(userGroup);
    }

    //删除用户组 —— 教师只能删自己创建的组，超管不限
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assertGroupOwner(id);
        userGroupService.removeById(id);
        return Result.success();
    }

    //获取用户组列表（分页）—— 教师的creatorId过滤以token身份为准，超管可看全部
    @GetMapping
    public Result<Page<UserGroupVO>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        // 关键词过滤与分页一并下推到SQL，不再先捞全量再在内存里筛选/切片
        Long creatorId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.getUserId();
        Page<UserGroupVO> page = new Page<>(pageNum, pageSize);
        userGroupService.listWithMemberCount(page, creatorId, keyword);
        return Result.success(page);
    }

    //获取用户组成员列表 —— 成员、创建者、超级管理员可见
    @GetMapping("/{id}/members")
    public Result<List<UserGroupMemberVO>> getMembers(@PathVariable Long id) {
        assertGroupVisible(id);
        return Result.success(userGroupService.getGroupDetail(id) != null ?
                userGroupService.getMembers(id) : new ArrayList<>());
    }

    //添加成员到用户组 —— 教师只能操作自己创建的组，超管不限
    @PostMapping("/{id}/member")
    public Result<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        assertGroupOwner(id);
        userGroupService.addMember(id, body.get("userId"));
        return Result.success();
    }

    //从用户组移除成员 —— 教师只能操作自己创建的组，超管不限
    @DeleteMapping("/{id}/member/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        assertGroupOwner(id);
        userGroupService.removeMember(id, userId);
        return Result.success();
    }


    //用户加入用户组（当前登录用户）
    @PostMapping("/join")
    public Result<Void> join(@RequestBody UserGroupMember member) {
        Long userId = SecurityUtils.getUserId();
        userGroupService.joinGroup(member.getUserGroupId(), userId);
        return Result.success();
    }

    //获取当前用户加入的用户组列表
    @GetMapping("/my-groups")
    public Result<List<UserGroup>> getMyGroups() {
        Long userId = SecurityUtils.getUserId();
        return Result.success(userGroupService.getMyGroups(userId));
    }

    //用户离开用户组（当前登录用户）
    @DeleteMapping("/leave")
    public Result<Void> leave(@RequestBody UserGroupMember member) {
        Long userId = SecurityUtils.getUserId();
        userGroupService.leaveGroup(member.getUserGroupId(), userId);
        return Result.success();
    }

    //刷新用户组分享码 —— 教师只能操作自己创建的组，超管不限
    @PutMapping("/{id}/share-code")
    public Result<UserGroup> refreshShareCode(@PathVariable Long id) {
        assertGroupOwner(id);
        UserGroup group = userGroupService.updateShareCode(id);
        if (group == null) {
            return Result.error("用户组不存在");
        }
        return Result.success(group);
    }

    //通过分享码加入用户组（当前登录用户）
    @PostMapping("/join-by-code")
    public Result<Void> joinByShareCode(@RequestBody Map<String, Object> body) {
        String shareCode = (String) body.get("shareCode");
        Long userId = SecurityUtils.getUserId();
        Boolean success = userGroupService.joinByShareCode(shareCode, userId);
        if (success) {
            return Result.success();
        }
        return Result.error("分享码无效或已加入该用户组");
    }

    //根据分享码获取用户组信息（isMember基于当前登录用户判断）
    @GetMapping("/by-share-code/{shareCode}")
    public Result<Map<String, Object>> getByShareCode(@PathVariable String shareCode) {
        UserGroup group = userGroupService.findByShareCode(shareCode);
        if (group == null) {
            return Result.error("未找到该分享码对应的用户组");
        }
        Long userId = SecurityUtils.getUserId();
        UserGroupVO vo = userGroupService.getGroupDetail(group.getId());
        boolean isMember = userGroupService.isMember(group.getId(), userId);
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", vo.getId());
        detail.put("name", vo.getName());
        detail.put("description", vo.getDescription());
        detail.put("shareCode", vo.getShareCode());
        detail.put("creatorId", vo.getCreatorId());
        detail.put("memberCount", vo.getMemberCount());
        detail.put("createTime", vo.getCreateTime());
        detail.put("updateTime", vo.getUpdateTime());
        detail.put("isMember", isMember);
        
        return Result.success(detail);
    }

    /**
     * 按JWT身份校验用户组归属：超级管理员放行，其余仅创建者可操作
     */
    private void assertGroupOwner(Long groupId) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        UserGroup group = userGroupService.getById(groupId);
        if (group == null || !Objects.equals(group.getCreatorId(), SecurityUtils.getUserId())) {
            throw new AccessDeniedException("只能操作自己创建的用户组");
        }
    }

    /**
     * 按JWT身份校验用户组可见性：创建者、成员、超级管理员可查看
     */
    private void assertGroupVisible(Long groupId) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        UserGroup group = userGroupService.getById(groupId);
        if (group == null) {
            throw new AccessDeniedException("用户组不存在");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (Objects.equals(group.getCreatorId(), currentUserId)) {
            return;
        }
        if (userGroupService.isMember(groupId, currentUserId)) {
            return;
        }
        throw new AccessDeniedException("无权查看该用户组");
    }
}
