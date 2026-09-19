package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.UserGroup;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;
import com.soft231.smartexam.entity.vo.UserGroupVO;

import java.util.List;

//用户组服务接口
//获取用户加入的用户组列表
//获取用户组列表（带成员数量）
//获取用户组列表按创建者（带成员数量）
//创建用户组
//更新用户组分享码
//根据分享码查找用户组
//获取用户组详情（带成员数量）
//获取用户组成员列表
//添加成员到用户组
//从用户组移除成员
//用户加入用户组
//用户离开用户组
//通过分享码加入用户组
//检查用户是否是用户组成员
public interface UserGroupService extends IService<UserGroup> {

    //获取用户加入的用户组列表
    List<UserGroup> getMyGroups(Long userId);

    //获取用户组列表（带成员数量，多表查询）
    List<UserGroupVO> listWithMemberCount();

    //获取用户组列表按创建者（带成员数量，多表查询）
    List<UserGroupVO> listWithMemberCount(Long creatorId);

    //创建用户组
    UserGroup createGroup(UserGroup group);

    //更新用户组分享码
    UserGroup updateShareCode(Long groupId);

    //根据分享码查找用户组
    UserGroup findByShareCode(String shareCode);

    //获取用户组详情（带成员数量，多表查询）
    UserGroupVO getGroupDetail(Long id);

    //获取用户组成员列表（多表JOIN查询）
    List<UserGroupMemberVO> getMembers(Long groupId);

    //添加成员到用户组
    void addMember(Long groupId, Long userId);

    //从用户组移除成员
    void removeMember(Long groupId, Long userId);

    //用户加入用户组
    void joinGroup(Long groupId, Long userId);

    //用户离开用户组
    void leaveGroup(Long groupId, Long userId);

    //通过分享码加入用户组
    Boolean joinByShareCode(String shareCode, Long userId);

    //检查用户是否是用户组成员
    boolean isMember(Long groupId, Long userId);
}