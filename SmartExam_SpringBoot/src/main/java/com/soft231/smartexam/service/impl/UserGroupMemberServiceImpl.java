package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.mapper.UserGroupMemberMapper;
import com.soft231.smartexam.service.UserGroupMemberService;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//用户组成员服务实现类
//成员操作：加入群组 joinGroup / 离开群组 leaveGroup / 获取成员列表 getMembersByGroupId
@Service
public class UserGroupMemberServiceImpl extends ServiceImpl<UserGroupMemberMapper, UserGroupMember> implements UserGroupMemberService {

    //用户加入用户组
    @Override
    public void joinGroup(UserGroupMember member) {
        member.setJoinTime(LocalDateTime.now());
        this.save(member);
    }

    //用户离开用户组
    @Override
    public void leaveGroup(UserGroupMember member) {
        LambdaQueryWrapper<UserGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserGroupMember::getUserId, member.getUserId())
               .eq(UserGroupMember::getUserGroupId, member.getUserGroupId());
        this.remove(wrapper);
    }

    //获取用户组的成员列表
    @Override
    public List<UserGroupMemberVO> getMembersByGroupId(Long groupId) {
        return baseMapper.selectMembersWithUserByGroupId(groupId);
    }
}