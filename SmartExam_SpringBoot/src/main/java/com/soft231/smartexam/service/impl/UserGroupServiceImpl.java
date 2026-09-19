package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.UserGroup;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.mapper.UserGroupMapper;
import com.soft231.smartexam.service.UserGroupMemberService;
import com.soft231.smartexam.service.UserGroupService;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;
import com.soft231.smartexam.entity.vo.UserGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

//用户组服务实现类
//群组CRUD：创建 createGroup / 详情 getGroupDetail / 列表 listWithMemberCount / 更新分享码 updateShareCode
//成员管理：获取成员列表 getMembers / 添加成员 addMember / 移除成员 removeMember / 检查成员身份 isMember
//加入离开：加入群组 joinGroup / 离开群组 leaveGroup / 通过分享码加入 joinByShareCode / 已加入群组列表 getMyGroups
//分享码查询：根据分享码查找 findByShareCode
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {

    @Autowired
    private UserGroupMemberService userGroupMemberService;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final Random RANDOM = new Random();

    private String generateShareCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateUniqueShareCode() {
        String code;
        do {
            code = generateShareCode();
        } while (this.lambdaQuery().eq(UserGroup::getShareCode, code).count() > 0);
        return code;
    }

    //获取用户加入的用户组列表
    @Override
    public List<UserGroup> getMyGroups(Long userId) {
        List<UserGroupMember> members = userGroupMemberService.lambdaQuery()
                .eq(UserGroupMember::getUserId, userId)
                .list();

        List<Long> groupIds = members.stream()
                .map(UserGroupMember::getUserGroupId)
                .collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            return List.of();
        }

        return this.listByIds(groupIds);
    }

    //获取用户组列表（带成员数量）
    @Override
    public List<UserGroupVO> listWithMemberCount() {
        return baseMapper.selectGroupsWithMemberCount();
    }

    //获取用户组列表按创建者（带成员数量）
    @Override
    public List<UserGroupVO> listWithMemberCount(Long creatorId) {
        if (creatorId == null) {
            return baseMapper.selectGroupsWithMemberCount();
        }
        return baseMapper.selectGroupsWithMemberCountByCreatorId(creatorId);
    }

    //创建用户组
    @Override
    public UserGroup createGroup(UserGroup group) {
        group.setShareCode(generateUniqueShareCode());
        this.save(group);
        return group;
    }

    //更新用户组分享码
    @Override
    public UserGroup updateShareCode(Long groupId) {
        UserGroup group = this.getById(groupId);
        if (group != null) {
            group.setShareCode(generateUniqueShareCode());
            this.updateById(group);
        }
        return group;
    }

    //根据分享码查找用户组
    @Override
    public UserGroup findByShareCode(String shareCode) {
        return this.lambdaQuery().eq(UserGroup::getShareCode, shareCode).one();
    }

    //获取用户组详情（带成员数量）
    @Override
    public UserGroupVO getGroupDetail(Long id) {
        return baseMapper.selectGroupWithMemberCountById(id);
    }

    //获取用户组成员列表
    @Override
    public List<UserGroupMemberVO> getMembers(Long groupId) {
        return userGroupMemberService.getMembersByGroupId(groupId);
    }

    //添加成员到用户组
    @Override
    public void addMember(Long groupId, Long userId) {
        joinGroup(groupId, userId);
    }

    //从用户组移除成员
    @Override
    public void removeMember(Long groupId, Long userId) {
        leaveGroup(groupId, userId);
    }

    //用户加入用户组
    @Override
    public void joinGroup(Long groupId, Long userId) {
        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(groupId);
        member.setUserId(userId);
        userGroupMemberService.joinGroup(member);
    }

    //用户离开用户组
    @Override
    public void leaveGroup(Long groupId, Long userId) {
        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(groupId);
        member.setUserId(userId);
        userGroupMemberService.leaveGroup(member);
    }

    //通过分享码加入用户组
    @Override
    public Boolean joinByShareCode(String shareCode, Long userId) {
        UserGroup group = this.findByShareCode(shareCode);
        if (group == null) {
            return false;
        }

        boolean isMember = userGroupMemberService.lambdaQuery()
                .eq(UserGroupMember::getUserGroupId, group.getId())
                .eq(UserGroupMember::getUserId, userId)
                .exists();

        if (isMember) {
            return false;
        }

        UserGroupMember member = new UserGroupMember();
        member.setUserGroupId(group.getId());
        member.setUserId(userId);
        userGroupMemberService.joinGroup(member);

        return true;
    }

    //检查用户是否是用户组成员
    @Override
    public boolean isMember(Long groupId, Long userId) {
        return userGroupMemberService.lambdaQuery()
                .eq(UserGroupMember::getUserGroupId, groupId)
                .eq(UserGroupMember::getUserId, userId)
                .exists();
    }
}