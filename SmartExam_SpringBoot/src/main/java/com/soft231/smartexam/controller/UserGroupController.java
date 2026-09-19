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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        UserGroup saved = userGroupService.createGroup(userGroup);
        return Result.success(saved);
    }

    //根据ID查询用户组详情
    @GetMapping("/{id}")
    public Result<UserGroupVO> getById(@PathVariable Long id) {
        UserGroupVO vo = userGroupService.getGroupDetail(id);
        if (vo == null) {
            return Result.error("用户组不存在");
        }
        return Result.success(vo);
    }

    //更新用户组信息
    @PutMapping
    public Result<UserGroup> update(@RequestBody UserGroup userGroup) {
        userGroupService.updateById(userGroup);
        return Result.success(userGroup);
    }

    //删除用户组
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userGroupService.removeById(id);
        return Result.success();
    }

    //获取用户组列表（分页）
    @GetMapping
    public Result<Page<UserGroupVO>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long creatorId
    ) {
        List<UserGroupVO> allData = userGroupService.listWithMemberCount(creatorId);

        if (StringUtils.hasText(keyword)) {
            allData = allData.stream()
                    .filter(item -> item.getName() != null && item.getName().contains(keyword))
                    .collect(Collectors.toList());
        }

        Page<UserGroupVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allData.size());

        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        page.setRecords(start < end ? allData.subList(start, end) : new ArrayList<>());

        return Result.success(page);
    }

    //获取用户组成员列表
    @GetMapping("/{id}/members")
    public Result<List<UserGroupMemberVO>> getMembers(@PathVariable Long id) {
        return Result.success(userGroupService.getGroupDetail(id) != null ?
                userGroupService.getMembers(id) : new ArrayList<>());
    }

    //添加成员到用户组
    @PostMapping("/{id}/member")
    public Result<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        userGroupService.addMember(id, body.get("userId"));
        return Result.success();
    }

    //从用户组移除成员
    @DeleteMapping("/{id}/member/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        userGroupService.removeMember(id, userId);
        return Result.success();
    }


    //用户加入用户组
    @PostMapping("/join")
    public Result<Void> join(@RequestBody UserGroupMember member) {
        userGroupService.joinGroup(member.getUserGroupId(), member.getUserId());
        return Result.success();
    }

    //获取用户加入的用户组列表
    @GetMapping("/my-groups/{userId}")
    public Result<List<UserGroup>> getMyGroups(@PathVariable Long userId) {
        return Result.success(userGroupService.getMyGroups(userId));
    }

    //用户离开用户组
    @DeleteMapping("/leave")
    public Result<Void> leave(@RequestBody UserGroupMember member) {
        userGroupService.leaveGroup(member.getUserGroupId(), member.getUserId());
        return Result.success();
    }

    //刷新用户组分享码
    @PutMapping("/{id}/share-code")
    public Result<UserGroup> refreshShareCode(@PathVariable Long id) {
        UserGroup group = userGroupService.updateShareCode(id);
        if (group == null) {
            return Result.error("用户组不存在");
        }
        return Result.success(group);
    }

    //通过分享码加入用户组
    @PostMapping("/join-by-code")
    public Result<Void> refreshShareCoderefreshShareCode(@RequestBody Map<String, Object> body) {
        String shareCode = (String) body.get("shareCode");
        Long userId = ((Number) body.get("userId")).longValue();
        Boolean success = userGroupService.joinByShareCode(shareCode, userId);
        if (success) {
            return Result.success();
        }
        return Result.error("分享码无效或已加入该用户组");
    }

    //根据分享码获取用户组信息
    @GetMapping("/by-share-code/{shareCode}")
    public Result<Map<String, Object>> getByShareCode(@PathVariable String shareCode, @RequestParam Long userId) {
        UserGroup group = userGroupService.findByShareCode(shareCode);
        if (group == null) {
            return Result.error("未找到该分享码对应的用户组");
        }
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
}
