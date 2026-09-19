package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;

import java.util.List;

//用户组成员服务接口
//用户加入用户组
//用户离开用户组
//获取用户组的成员列表
public interface UserGroupMemberService extends IService<UserGroupMember> {

    //用户加入用户组
    void joinGroup(UserGroupMember member);

    //用户离开用户组
    void leaveGroup(UserGroupMember member);

    //获取用户组的成员列表（多表JOIN查询）
    List<UserGroupMemberVO> getMembersByGroupId(Long groupId);
}